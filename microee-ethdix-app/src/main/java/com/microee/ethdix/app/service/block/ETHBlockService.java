package com.microee.ethdix.app.service.block;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.microee.ethdix.app.components.ETHBlockShard;
import com.microee.ethdix.app.components.Web3JFactory;
import com.microee.ethdix.oem.eth.EthRawBlock;
import com.microee.ethdix.oem.eth.EthRawTransaction;
import com.microee.ethdix.oem.eth.enums.ChainId;
import com.microee.plugin.thread.ThreadPoolFactoryLow;
import com.microee.stacks.mongodb.support.Mongo;

@Service
public class ETHBlockService {

    private static ThreadPoolFactoryLow threadPool = ThreadPoolFactoryLow.newInstance("ethblock-查询区块交易回执线程池");

    // db.eth_blocks.createIndex( { _id: -1 }, { background: true } )
    public static final String COLLECTION_BLOCKS = "eth_blocks";

    @Autowired
    private ETHBlockShard ethBlockShard;
    
    @Autowired
    private Mongo mongo;

    @Autowired
    private ETHReceiptService txReceiptService;

    @Autowired
    private ETHTransService ethTransService;

    @Autowired
    private Web3JFactory web3JFactory;

    // 查询并保存区块
    public EthRawBlock ethGetBlockByNumber(String ethnode, ChainId chainId, Long blockNumber) {
        if (blockNumber != null && blockNumber < 0) {
            return null;
        }
        String blockCollectionName = ethBlockShard.getCollection(COLLECTION_BLOCKS, blockNumber);
        EthRawBlock result = mongo.queryById(blockCollectionName, blockNumber, EthRawBlock.class);
        if (result == null) {
            result = web3JFactory.getJsonRpc(chainId, ethnode).getBlockByNumber(blockNumber);
            if ((ethnode == null || ethnode.isEmpty()) && result != null) {
                mongo.save(blockCollectionName, result, blockNumber, "transactions"); // 交易信息保存到另一个表
                // mongo.save(ethNetworkProperties.getCollectionName(COLLECTION_BLOCKS, blockNumber), result, blockNumber); 
                ethTransService.saveTransactions(blockNumber, result.getTransactions());
            }
        }
        if ((ethnode == null || ethnode.isEmpty()) && result != null) {
            // 懒加载交易回执
            List<EthRawTransaction> trans = result.getTransactions();
            if (trans != null && trans.size() > 0) {
                List<String> currentTransHashList = trans.stream().map(m -> m.getHash()).collect(Collectors.toList());
                List<String> transHashList = mongo.notIn(ETHReceiptService.COLLECTION_NAME, currentTransHashList);
                if (transHashList.size() > 0) {
                    threadPool.pool().submit(() -> {
                        for (int i = 0; i < transHashList.size(); i++) {
                            final String currentTranHash = transHashList.get(i);
                            txReceiptService.getTransactionReceipt(ethnode, chainId, blockNumber, currentTranHash);
                        }
                    });
                }
            }
        }
        if (result != null) {
            result.setTransactions(ethTransService.getTransactionsByBlockNumber(ethnode, blockNumber));
        }
        return result;
    }

    // 找出不连续的区块id
    public List<Long> ethBreakBlockNumber(String collectionName, Long start, Long end) {
        List<Long> blockList = mongo.between(collectionName, start, end);
        List<Long> result = new LinkedList<>();
        if (blockList.isEmpty()) {
            result.add(start);
            return result;
        }
        Set<Long> listSet = new HashSet<>(blockList);
        listSet.stream().filter(l -> (!listSet.contains(l + 1))).forEachOrdered(l -> {
            result.add(l + 1);
        });
        result.sort((l1, l2) -> l1.compareTo(l2));
        return result;
    }

}
