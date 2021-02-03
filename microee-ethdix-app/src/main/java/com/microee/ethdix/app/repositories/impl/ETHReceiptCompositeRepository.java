package com.microee.ethdix.app.repositories.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.microee.ethdix.app.components.ETHBlockShard;
import com.microee.ethdix.app.repositories.IETHReceiptRepository;
import com.microee.ethdix.oem.eth.EthTransactionReceipt;
import com.microee.ethdix.oem.eth.enums.ChainId;
import com.microee.stacks.mongodb.support.Mongo;

@Service
public class ETHReceiptCompositeRepository implements IETHReceiptRepository {

    public static final String MONGODB_COLLECTION_NAME = "receipts";
    
    @Autowired(required = false)
    private Mongo mongo;
    
    @Autowired
    private ETHBlockShard ethBlockShard;
    
    @Override
    public EthTransactionReceipt queryReceiptsByStringId(ChainId chainId, Long blockNumber, String txHash) {
        String receiptCollectionName = ethBlockShard.getCollection(chainId, MONGODB_COLLECTION_NAME, blockNumber);
        if (mongo != null) {
            return mongo.queryByStringId(receiptCollectionName, txHash, EthTransactionReceipt.class);
        }
        return null;
    }

    @Override
    public void saveReceipts(ChainId chainId, Long blockNumber, EthTransactionReceipt receipt, String txHash) {
        String receiptCollectionName = ethBlockShard.getCollection(chainId, MONGODB_COLLECTION_NAME, blockNumber);
        if (mongo != null) {
            mongo.save(receiptCollectionName, receipt, txHash);
        }
    } 

    @Override
    public List<String> notStoredReceipts(List<String> currentTransHashList) {
        // TODO
        return currentTransHashList;
    }
    
}
