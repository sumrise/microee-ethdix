package com.microee.ethdix.app.listeners;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.microee.ethdix.app.components.NewBlockProcess;
import com.microee.ethdix.j3.wss.ETHMessageListener;
import com.microee.ethdix.oem.eth.enums.ChainId;
import okhttp3.HttpUrl;

@Component
public class ETHBlockListener implements ETHMessageListener {

    @Autowired
    private NewBlockProcess newBlockProcess;
    
    @Override
    public void onNewBlock(ChainId chainId, HttpUrl endpoint, Long blockNumber, Long timestamp) {
        newBlockProcess.onProcessNewBlock(chainId, endpoint, blockNumber, timestamp);
    }

}
