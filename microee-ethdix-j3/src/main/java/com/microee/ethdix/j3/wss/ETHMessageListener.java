package com.microee.ethdix.j3.wss;

import okhttp3.HttpUrl;

public interface ETHMessageListener {

    public void onNewBlock(HttpUrl endpoint, Long blockNumber, Long timestamp);
}
