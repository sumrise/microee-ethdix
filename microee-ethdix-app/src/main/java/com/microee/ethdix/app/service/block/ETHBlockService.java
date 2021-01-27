package com.microee.ethdix.app.service.block;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
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
    public static final String COLLECTION_BLOCKS = "blocks";

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
    public EthRawBlock ethGetBlockByNumber(String ethnode, ChainId chainId, Long blockNumber, boolean fanout) {
        if (blockNumber != null && blockNumber < 0) {
            return null;
        }
        String blockCollectionName = blockNumber == null ? null : ethBlockShard.getCollection(chainId, COLLECTION_BLOCKS, blockNumber);
        if (blockCollectionName != null && !fanout) {
            // 从数据库查
            EthRawBlock cachedResult = mongo.queryById(blockCollectionName, blockNumber, EthRawBlock.class);
            if (cachedResult != null) {
                cachedResult.setTransactions(ethTransService.getTransactionsByBlockNumber(ethnode, chainId, blockNumber));
                lazyTransactionReceipt(cachedResult, ethnode, chainId, blockNumber);
                return cachedResult;
            }
        }
        // 数据库没查到，查链
        EthRawBlock fanoutResult = blockNumber == null ? null : web3JFactory.getJsonRpc(chainId, ethnode).getBlockByNumber(blockNumber);
        if (blockCollectionName != null && fanoutResult != null) {
            mongo.save(blockCollectionName, fanoutResult, blockNumber, "transactions"); // 交易信息保存到另一个表
            ethTransService.saveTransactions(chainId, blockNumber, fanoutResult.getTransactions());
            lazyTransactionReceipt(fanoutResult, ethnode, chainId, blockNumber);
        }
        return fanoutResult;
    }
    
    // 懒加载交易回执
    public void lazyTransactionReceipt(@NotNull EthRawBlock block, String ethnode, ChainId chainId, Long blockNumber) {
        List<EthRawTransaction> trans = block.getTransactions();
        if (trans != null && trans.size() > 0) {
            List<String> currentTransHashList = trans.stream().map(m -> m.getHash()).collect(Collectors.toList());
            List<String> transHashList = mongo.notIn(ETHReceiptService.COLLECTION_NAME, currentTransHashList);
            if (transHashList.size() > 0) {
                threadPool.submit(() -> {
                    for (int i = 0; i < transHashList.size(); i++) {
                        txReceiptService.getTransactionReceipt(ethnode, chainId, blockNumber, transHashList.get(i));
                    }
                });
            }
        }
    }

    // 根据区块哈希取得区块编号
    public EthRawBlock ethGetBlockByHash(String ethnode, ChainId chainId, String blockHash) {
        return web3JFactory.getJsonRpc(chainId, ethnode).getBlockByHash(blockHash);
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

    // 当前区块是否是孤块
    public Boolean ethBlockLonely(String ethnode, ChainId chainId, EthRawBlock block) {
        Long currentBlockNumber = Long.parseLong(block.getNumber().substring(2), 16);
        EthRawBlock parentBlock = this.ethGetBlockByNumber(ethnode, chainId, currentBlockNumber - 1, false);
        if (parentBlock.getHash().equalsIgnoreCase(block.getParentHash())) {
            return false;
        }
        return true;
    }

}
