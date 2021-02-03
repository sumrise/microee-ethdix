package com.microee.ethdix.app.repositories;

import java.util.List;
import com.microee.ethdix.oem.eth.EthRawTransaction;
import com.microee.ethdix.oem.eth.enums.ChainId;

public interface IETHTransRepository {

    EthRawTransaction queryTxsByStringId(ChainId chainId, Long blockNumber, String transHash);

    public void saveTransaction(ChainId chainId, Long blockNumber, EthRawTransaction tx);
    
    List<EthRawTransaction> getTransactionsByBlockNumber(ChainId chainId, Long blockNumber);

    void saveTransactions(ChainId chainId, Long blockNumber, List<EthRawTransaction> transactions);
}
