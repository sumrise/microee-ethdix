package com.microee.ethdix.app.service.block;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.microee.ethdix.app.components.Web3JFactory;
import com.microee.ethdix.app.repositories.IETHBlockRepository;
import com.microee.ethdix.oem.eth.EthRawBlock;
import com.microee.ethdix.oem.eth.enums.ChainId;

@Service
public class ETHBlockService {

    // db.eth_blocks.createIndex( { _id: -1 }, { background: true } )
    public static final String COLLECTION_BLOCKS = "blocks";

    @Autowired
    private IETHBlockRepository ethBlockRepository;
    
    @Autowired
    private ETHReceiptService txReceiptService;

    @Autowired
    private ETHTransService ethTransService;

    @Autowired
    private Web3JFactory web3JFactory;

    // 查询并保存区块
    public EthRawBlock ethGetBlockByNumber(ChainId chainId, Long blockNumber) {
        return this.ethGetBlockByNumber(null, chainId, blockNumber, true);
    }
    
    // 查询并保存区块
    public EthRawBlock ethGetBlockByNumber(String ethnode, ChainId chainId, Long blockNumber, boolean fanout) {
        if (!fanout) {
            // 从数据库查
            EthRawBlock cachedResult = ethBlockRepository.queryBlockById(chainId, blockNumber); 
            if (cachedResult != null) {
                cachedResult.setTransactions(ethTransService.getTransactionsByBlockNumber(ethnode, chainId, blockNumber));
                txReceiptService.lazyTransactionReceipt(cachedResult, ethnode, chainId, blockNumber);
                return cachedResult;
            }
        }
        // 数据库没查到，查链
        EthRawBlock fanoutResult = web3JFactory.getJsonRpc(chainId, ethnode).getBlockByNumber(blockNumber);
        if (fanoutResult != null) {
            ethBlockRepository.saveBlock(chainId, blockNumber, fanoutResult);
            ethTransService.saveTransactions(chainId, blockNumber, fanoutResult.getTransactions());
            txReceiptService.lazyTransactionReceipt(fanoutResult, ethnode, chainId, blockNumber);
        }
        return fanoutResult;
    }
    

    // 根据区块哈希取得区块编号
    public EthRawBlock ethGetBlockByHash(String ethnode, ChainId chainId, String blockHash) {
        return web3JFactory.getJsonRpc(chainId, ethnode).getBlockByHash(blockHash);
    }

    // 找出不连续的区块id
    public List<Long> ethBreakBlockNumber(String collectionName, Long start, Long end) {
        List<Long> blockList = ethBlockRepository.between(collectionName, start, end);
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
