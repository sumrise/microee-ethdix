package com.microee.ethdix.app.listeners;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.microee.ethdix.app.service.JsonRpcHttpClientLogService;
import com.microee.ethdix.j3.rpc.IJsonRpcHttpClientListener;
import com.microee.ethdix.oem.eth.enums.ChainId;

@Component
public class JsonRpcHttpClientListener implements IJsonRpcHttpClientListener {

    @Autowired
    private JsonRpcHttpClientLogService jsonRpcHttpClientLogService;
    
    @Override
    public void log(ChainId chainId, boolean success, int statusCode, long contentLength, String method, String url, String message, Long start, Long speed) {
        jsonRpcHttpClientLogService.save(chainId, success, statusCode, contentLength, method, url, message, start, speed); 
    }

}
