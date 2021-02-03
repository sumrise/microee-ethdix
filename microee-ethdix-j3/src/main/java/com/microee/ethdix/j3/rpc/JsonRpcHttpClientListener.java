package com.microee.ethdix.j3.rpc;

import com.microee.ethdix.oem.eth.enums.ChainId;
import com.microee.plugin.http.assets.HttpClientLogger;
import okhttp3.Headers;

public class JsonRpcHttpClientListener implements HttpClientLogger {

    private ChainId chainId;
    private IJsonRpcHttpClientListener jsonRpcHttpClientListener;
    
    public JsonRpcHttpClientListener (ChainId chainId, IJsonRpcHttpClientListener jsonRpcHttpClientListener) {
        this.chainId = chainId;
        this.jsonRpcHttpClientListener = jsonRpcHttpClientListener;
    }
    
    @Override
    public void log(
            boolean success, int statusCode, long contentLength, String method, String url,
            Headers headers, String bodyString, String message, Long start, Long speed,
            Boolean isSSl, String proxyString) {
        this.jsonRpcHttpClientListener.log(chainId, success, statusCode, contentLength, method, url, message, start, speed);
    }

}
