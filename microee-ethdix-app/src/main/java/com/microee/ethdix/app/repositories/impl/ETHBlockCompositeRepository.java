package com.microee.ethdix.app.repositories.impl;

import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.microee.ethdix.app.components.ETHBlockShard;
import com.microee.ethdix.app.repositories.IETHBlockRepository;
import com.microee.ethdix.oem.eth.EthRawBlock;
import com.microee.ethdix.oem.eth.enums.ChainId;
import com.microee.stacks.mongodb.support.Mongo;

@Service
public class ETHBlockCompositeRepository implements IETHBlockRepository {

    // db.eth_blocks.createIndex( { _id: -1 }, { background: true } )
    public static final String MONGODB_COLLECTION_BLOCKS = "blocks";

    @Autowired(required = false)
    private Mongo mongo;

    @Autowired
    private ETHBlockShard ethBlockShard;

    @Override
    public EthRawBlock queryBlockById(ChainId chainId, @NotNull Long blockNumber) {
        final String blockCollectionName =
                ethBlockShard.getCollection(chainId, MONGODB_COLLECTION_BLOCKS, blockNumber);
        if (blockCollectionName == null) {
            return null;
        }
        if (mongo != null) {
            return mongo.queryById(blockCollectionName, blockNumber, EthRawBlock.class);
        }
        return null;
    }

    @Override
    public Boolean saveBlock(ChainId chainId, Long blockNumber, EthRawBlock block) { 
        final String blockCollectionName =
                ethBlockShard.getCollection(chainId, MONGODB_COLLECTION_BLOCKS, blockNumber);
        if (blockCollectionName == null) {
            throw new RuntimeException("未能取得分表名字");
        }
        if (mongo != null) {
            mongo.save(blockCollectionName, block, blockNumber, "transactions"); // 交易信息保存到另一个表
            return false;
        }
        return true;
    }

    @Override
    public List<Long> between(String collectionName, Long start, Long end) {
        // TODO Auto-generated method stub
        return null;
    }

}
