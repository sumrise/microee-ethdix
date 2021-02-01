package com.microee.ethdix.j3.wss;

import com.microee.ethdix.oem.eth.enums.ChainId;
import okhttp3.HttpUrl;

public interface ETHMessageListener {

    public void onNewBlock(ChainId chainId, HttpUrl endpoint, Long blockNumber, Long timestamp);
}
