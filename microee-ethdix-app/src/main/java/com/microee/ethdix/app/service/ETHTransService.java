package com.microee.ethdix.app.service;

import com.microee.ethdix.app.components.Web3JFactory;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import com.microee.ethdix.app.props.ETHNetworkProperties;
import com.microee.ethdix.oem.eth.EthRawTransaction;
import com.microee.stacks.mongodb.support.Mongo;

@Service
public class ETHTransService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ETHTransService.class);

    public static final String COLLECTION_TRANS = "eth_blocks_trans";

    @Autowired
    private Web3JFactory web3JFactory;

    @Autowired
    private ETHNetworkProperties ethNetworkProperties;

    @Autowired
    private Mongo mongo;

    // 查询并保存交易基本信息
    public EthRawTransaction ethGetTransaction(String ethnode, String network, Long blockNumber, String transHash) {
        EthRawTransaction result = mongo.queryByStringId(ethNetworkProperties.getCollectionName(COLLECTION_TRANS, blockNumber), transHash, EthRawTransaction.class);
        if (result == null) {
            result = web3JFactory.getJsonRpc(network, ethnode).getTransactionByHash(transHash);
            if ((ethnode == null || ethnode.isEmpty()) && result != null) {
                if (result.getBlockNumber() != null) {
                    mongo.save(ethNetworkProperties.getCollectionName(COLLECTION_TRANS, blockNumber), result, transHash);
                }
            }
        }
        return result;
    }

    // 根据区块编号查询该区块上的所有交易
    public List<EthRawTransaction> getTransactionsByBlockNumber(String ethNode, Long blockNumber) {
        Query query = Query.query(Criteria.where("blockNumber").is("0x" + Long.toHexString(blockNumber)));
        return mongo.queryList(ethNetworkProperties.getCollectionName(COLLECTION_TRANS, blockNumber), query, EthRawTransaction.class);
    }

    // 保存区块上的所有交易记录
    public boolean saveTransactions(Long blockNumber, List<EthRawTransaction> transactions) {
        if (transactions != null && transactions.size() > 0) {
            mongo.saveList(ethNetworkProperties.getCollectionName(COLLECTION_TRANS, blockNumber), "hash", transactions);
        }
        return true;
    }

    // 查询指定地址的转入记录
    public List<EthRawTransaction> queryTransferTo(String hashAddress) {
        Query query = Query.query(Criteria.where("to").is(hashAddress));
        List<String> collectionList = ethNetworkProperties.getCollectionNamesByBlockNumber(null);
        List<EthRawTransaction> resultList = new ArrayList<>();
        collectionList.stream().map(collectionKey -> ethNetworkProperties.getCollectionName(ETHTransService.COLLECTION_TRANS, collectionKey)).map(collectionName -> {
            Boolean collectionExists = mongo.collectionExists(collectionName);
            LOGGER.info("collectionExists={}", collectionExists);
            List<EthRawTransaction> currentList = mongo.queryList(collectionName, query, EthRawTransaction.class);
            return currentList;
        }).filter(currentList -> (currentList.size() > 0)).forEachOrdered(currentList -> {
            resultList.addAll(currentList);
        });
        return resultList;
    }

    // 查询指定地址的转出记录
    public List<EthRawTransaction> queryTransferFrom(String hashAddress) {
        Query query = Query.query(Criteria.where("from").is(hashAddress));
        List<String> collectionList = ethNetworkProperties.getCollectionNamesByBlockNumber(null);
        List<EthRawTransaction> resultList = new ArrayList<>();
        collectionList.stream().map(collectionKey -> ethNetworkProperties.getCollectionName(ETHTransService.COLLECTION_TRANS, collectionKey)).map(collectionName -> {
            Boolean collectionExists = mongo.collectionExists(collectionName);
            LOGGER.info("collectionExists={}", collectionExists);
            List<EthRawTransaction> currentList = mongo.queryList(collectionName, query, EthRawTransaction.class);
            return currentList;
        }).filter(currentList -> (currentList.size() > 0)).forEachOrdered(currentList -> {
            resultList.addAll(currentList);
        });
        return resultList;
    }

}
