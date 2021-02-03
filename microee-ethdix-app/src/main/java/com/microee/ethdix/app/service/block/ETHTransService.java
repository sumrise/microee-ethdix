package com.microee.ethdix.app.service.block;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import com.microee.ethdix.app.components.ETHBlockShard;
import com.microee.ethdix.app.components.Web3JFactory;
import com.microee.ethdix.oem.eth.EthRawTransaction;
import com.microee.ethdix.oem.eth.enums.ChainId;
import com.microee.stacks.mongodb.support.Mongo;

@Service
public class ETHTransService {

    public static final String COLLECTION_TRANS = "trans";

    @Autowired
    private Web3JFactory web3JFactory;

    @Autowired(required=false)
    private Mongo mongo;

    @Autowired
    private ETHBlockShard ethBlockShard;
    
    // 查询并保存交易基本信息
    public EthRawTransaction ethGetTransaction(String ethnode, ChainId chainId, Long blockNumber, String transHash) {
        String transCollectionName = ethBlockShard.getCollection(chainId, COLLECTION_TRANS, blockNumber);
        EthRawTransaction result = mongo == null ? null : mongo.queryByStringId(transCollectionName, transHash, EthRawTransaction.class);
        if (result == null) {
            result = web3JFactory.getJsonRpc(chainId, ethnode).getTransactionByHash(transHash);
            if ((ethnode == null || ethnode.isEmpty()) && result != null) {
                if (result.getBlockNumber() != null) {
                    if (mongo != null) {
                        mongo.save(transCollectionName, result, transHash);
                    }
                }
            }
        }
        return result;
    }

    // 根据区块编号查询该区块上的所有交易
    public List<EthRawTransaction> getTransactionsByBlockNumber(String ethNode, ChainId chainId, Long blockNumber) {
        if (mongo == null) {
            return new ArrayList<>();
        }
        String transCollectionName = ethBlockShard.getCollection(chainId, COLLECTION_TRANS, blockNumber);
        Query query = Query.query(Criteria.where("blockNumber").is("0x" + Long.toHexString(blockNumber)));
        return mongo.queryList(transCollectionName, query, EthRawTransaction.class);
    }

    // 保存区块上的所有交易记录
    public boolean saveTransactions(ChainId chainId, Long blockNumber, List<EthRawTransaction> transactions) {
        if (mongo == null) {
            return true;
        }
        if (transactions != null && transactions.size() > 0) {
            String transCollectionName = ethBlockShard.getCollection(chainId, COLLECTION_TRANS, blockNumber);
            mongo.saveList(transCollectionName, "hash", transactions);
        }
        return true;
    }

    // 查询指定地址的转入记录
    public List<EthRawTransaction> queryTransferTo(String hashAddress) {
//        String transCollectionName = ethBlockShard.getCollection(COLLECTION_TRANS, null);
//        Query query = Query.query(Criteria.where("to").is(hashAddress));
//        List<String> collectionList = ethNetworkProperties.getCollectionNamesByBlockNumber(null);
//        List<EthRawTransaction> resultList = new ArrayList<>();
//        collectionList.stream().map(collectionKey -> ethNetworkProperties.getCollectionName(ETHTransService.COLLECTION_TRANS, collectionKey)).map(collectionName -> {
//            Boolean collectionExists = mongo.collectionExists(collectionName);
//            LOGGER.info("collectionExists={}", collectionExists);
//            List<EthRawTransaction> currentList = mongo.queryList(collectionName, query, EthRawTransaction.class);
//            return currentList;
//        }).filter(currentList -> (currentList.size() > 0)).forEachOrdered(currentList -> {
//            resultList.addAll(currentList);
//        });
        return null;
    }

    // 查询指定地址的转出记录
    public List<EthRawTransaction> queryTransferFrom(String hashAddress) {
//        Query query = Query.query(Criteria.where("from").is(hashAddress));
//        List<String> collectionList = ethNetworkProperties.getCollectionNamesByBlockNumber(null);
//        List<EthRawTransaction> resultList = new ArrayList<>();
//        collectionList.stream().map(collectionKey -> ethNetworkProperties.getCollectionName(ETHTransService.COLLECTION_TRANS, collectionKey)).map(collectionName -> {
//            Boolean collectionExists = mongo.collectionExists(collectionName);
//            LOGGER.info("collectionExists={}", collectionExists);
//            List<EthRawTransaction> currentList = mongo.queryList(collectionName, query, EthRawTransaction.class);
//            return currentList;
//        }).filter(currentList -> (currentList.size() > 0)).forEachOrdered(currentList -> {
//            resultList.addAll(currentList);
//        });
        return null;
    }

}
