package com.microee.ethdix.app.repositories.impl;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import com.microee.ethdix.app.components.ETHBlockShard;
import com.microee.ethdix.app.repositories.IETHTransRepository;
import com.microee.ethdix.oem.eth.EthRawTransaction;
import com.microee.ethdix.oem.eth.enums.ChainId;
import com.microee.stacks.mongodb.support.Mongo;

@Service
public class ETHTransCompositeRepository implements IETHTransRepository {

    private static final Logger logger = LoggerFactory.getLogger(ETHTransCompositeRepository.class);
    
    public static final String MONGODB_COLLECTION_TRANS = "trans";

    @Autowired(required = false)
    private Mongo mongo;
    
    @Autowired
    private ETHBlockShard ethBlockShard;
    
    @Override
    public EthRawTransaction queryTxsByStringId(ChainId chainId, Long blockNumber, String transHash) {
        String transCollectionName = ethBlockShard.getCollection(chainId, MONGODB_COLLECTION_TRANS, blockNumber);
        if (mongo != null) {
            return mongo.queryByStringId(transCollectionName, transHash, EthRawTransaction.class);
        }
        return null;
    }

    @Override
    public void saveTransaction(ChainId chainId, Long blockNumber, EthRawTransaction tx) {
        String transCollectionName = ethBlockShard.getCollection(chainId, MONGODB_COLLECTION_TRANS, blockNumber);
        if (mongo != null) {
            mongo.save(transCollectionName, tx, tx.getHash()); 
        }
    }

    @Override
    public List<EthRawTransaction> getTransactionsByBlockNumber(ChainId chainId, Long blockNumber) {
        String transCollectionName = ethBlockShard.getCollection(chainId, MONGODB_COLLECTION_TRANS, blockNumber);
        if (mongo != null) {
            Query query = Query.query(Criteria.where("blockNumber").is("0x" + Long.toHexString(blockNumber)));
            return mongo.queryList(transCollectionName, query, EthRawTransaction.class);
        }
        return null;
    }

    @Override
    public void saveTransactions(ChainId chainId, Long blockNumber, List<EthRawTransaction> transactions) {
        String transCollectionName = ethBlockShard.getCollection(chainId, MONGODB_COLLECTION_TRANS, blockNumber);
        if (mongo != null) {
            try {
                mongo.saveList(transCollectionName, "hash", transactions);
            } catch (DuplicateKeyException e) {
                logger.error("交易主键冲突: blockNumber={}, errorMessage={}", blockNumber, e.getMessage(), e);
            }
        }
    }

    
}
