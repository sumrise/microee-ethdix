package com.microee.ethdix.app.listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.microee.ethdix.j3.rpc.IJsonRpcHttpClientListener;
import com.microee.ethdix.oem.eth.enums.ChainId;

@Component
public class JsonRpcHttpClientListener implements IJsonRpcHttpClientListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonRpcHttpClientListener.class);

    @Override
    public void log(ChainId chainId, boolean success, int statusCode, long contentLength, String method, String url, String message, Long start, Long speed) {
        LOGGER.info("jsonrpc: chainId={}, url={}, method={}, code={}, success={}, start={}, speed={}, message={}", chainId.name, url, method, statusCode, success, start, speed, message);
    }

}
