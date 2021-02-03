package com.microee.ethdix.app.repositories;

import java.util.List;
import com.microee.ethdix.oem.eth.EthTransactionReceipt;
import com.microee.ethdix.oem.eth.enums.ChainId;

public interface IETHReceiptRepository {

    EthTransactionReceipt queryReceiptsByStringId(ChainId chainId, Long blockNumber, String txHash);

    void saveReceipts(ChainId chainId, Long blockNumber, EthTransactionReceipt result, String txHash);

    public List<String> notStoredReceipts(List<String> currentTransHashList);


}
