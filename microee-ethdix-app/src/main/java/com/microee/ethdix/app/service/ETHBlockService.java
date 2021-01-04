package com.microee.ethdix.app.service;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import com.microee.ethdix.app.props.ETHNetworkProperties;
import com.microee.ethdix.j3.rpc.JsonRPC;
import com.microee.ethdix.oem.eth.EthRawBlock;
import com.microee.ethdix.oem.eth.EthRawTransaction;
import com.microee.plugin.thread.ThreadPoolFactoryLow;
import com.microee.stacks.mongodb.support.Mongo;

@Service
public class ETHBlockService {

    private static ThreadPoolFactoryLow threadPool =
            ThreadPoolFactoryLow.newInstance("ethblock-查询区块交易回执线程池");

    // db.eth_blocks.createIndex( { _id: -1 }, { background: true } )
    public static final String COLLECTION_BLOCKS = "eth_blocks";

    @Autowired
    private ETHNetworkProperties ethNetworkProperties;
    
    @Autowired
    private Mongo mongo;

    @Autowired
    private ETHBlockTxReceiptService txReceiptService;

    @Autowired
    private ETHTransService ethTransService;

    @Autowired
	@Qualifier("jsonRPCClientMainnet")
    private JsonRPC jsonRPCClient;

    // 查询并保存区块
    public EthRawBlock ethGetBlockByNumber(String ethNode, Long blockNumber) {
        if (blockNumber < 0) {
            return null;
        }
        EthRawBlock result = mongo.queryById(ethNetworkProperties.getCollectionName(COLLECTION_BLOCKS, blockNumber), blockNumber, EthRawBlock.class);
        if (result == null) {
            result = this.jsonRPCClient.getBlockByNumber(ethNode, blockNumber);
            if ((ethNode == null || ethNode.isEmpty()) && result != null) {
                mongo.save(ethNetworkProperties.getCollectionName(COLLECTION_BLOCKS, blockNumber), result, blockNumber, "transactions"); // remove 0x, 交易信息保存到令一个表
                ethTransService.saveTransactions(blockNumber, result.getTransactions());
            }
        }
        if ((ethNode == null || ethNode.isEmpty()) && result != null) {
            // 懒加载交易回执
            List<EthRawTransaction> trans = result.getTransactions();
            if (trans != null && trans.size() > 0) {
                List<String> currentTransHashList =
                        trans.stream().map(m -> m.getHash()).collect(Collectors.toList());
                List<String> transHashList =
                        mongo.notIn(ETHBlockTxReceiptService.COLLECTION_NAME, currentTransHashList);
                if (transHashList.size() > 0) {
                    threadPool.pool().submit(() -> {
                        for (int i = 0; i < transHashList.size(); i++) {
                            final String currentTranHash = transHashList.get(i);
                            txReceiptService.getTransactionReceipt(ethNode, blockNumber, currentTranHash);
                        }
                    });
                }
            }
        }
        if (result != null) {
            result.setTransactions(ethTransService.getTransactionsByBlockNumber(ethNode, blockNumber));
        }
        return result;
    }

    // 找出不连续的区块id
    public List<Long> ethBreakBlockNumber(String collectionName, Long start, Long end) {
        List<Long> blockList = mongo.between(collectionName, start, end);
        List<Long> result = new LinkedList<>();
        if (blockList.size() == 0) {
            result.add(start);
            return result;
        }
        Set<Long> listSet = new HashSet<>(blockList);
        for (Long l : listSet) {
            if (!listSet.contains(l + 1)) {
                result.add(l + 1);
            }
        }
        result.sort((l1, l2) -> l1.compareTo(l2));
        return result;
    }

}
