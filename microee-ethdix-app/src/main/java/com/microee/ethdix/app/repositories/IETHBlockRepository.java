package com.microee.ethdix.app.repositories;

import java.util.List;
import com.microee.ethdix.oem.eth.EthRawBlock;
import com.microee.ethdix.oem.eth.enums.ChainId;

public interface IETHBlockRepository {

    public EthRawBlock queryBlockById(ChainId chainId, Long blockNumber);
    
    public Boolean saveBlock(ChainId chainId, Long blockNumber, EthRawBlock block);
    
    public List<Long> between(String collectionName, Long start, Long end);
    
}
