package com.microee.ethdix.app.service.block;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.microee.ethdix.app.components.Web3JFactory;
import com.microee.ethdix.app.repositories.IETHTransRepository;
import com.microee.ethdix.oem.eth.EthRawTransaction;
import com.microee.ethdix.oem.eth.enums.ChainId;

@Service
public class ETHTransService {

    @Autowired
    private Web3JFactory web3JFactory;

    @Autowired
    private IETHTransRepository ethTransRepository;
    
    // 查询并保存交易基本信息
    public EthRawTransaction ethGetTransaction(String ethnode, ChainId chainId, Long blockNumber, String transHash) {
        EthRawTransaction result = ethTransRepository.queryTxsByStringId(chainId, blockNumber, transHash);
        if (result == null) {
            result = web3JFactory.getJsonRpc(chainId, ethnode).getTransactionByHash(transHash);
            if ((ethnode == null || ethnode.isEmpty()) && result != null) {
                if (result.getBlockNumber() != null) {
                    ethTransRepository.saveTransaction(chainId, blockNumber, result);
                }
            }
        }
        return result;
    }

    // 根据区块编号查询该区块上的所有交易
    public List<EthRawTransaction> getTransactionsByBlockNumber(String ethNode, ChainId chainId, Long blockNumber) {
        return ethTransRepository.getTransactionsByBlockNumber(chainId, blockNumber);
    }

    // 保存区块上的所有交易记录
    public boolean saveTransactions(ChainId chainId, Long blockNumber, List<EthRawTransaction> transactions) {
        if (transactions != null && transactions.size() > 0) {
            ethTransRepository.saveTransactions(chainId, blockNumber, transactions); 
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
