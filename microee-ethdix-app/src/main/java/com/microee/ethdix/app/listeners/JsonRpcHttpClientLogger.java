package com.microee.ethdix.app.listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.microee.plugin.http.assets.HttpClientLogger;
import okhttp3.Headers;

@Component
public class JsonRpcHttpClientLogger implements HttpClientLogger {

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonRpcHttpClientLogger.class);
    
    @Override
    public void log(
            boolean success, int statusCode, long contentLength, 
            String method, String url, Headers headers, String bodyString, String message,
            Long start, Long speed, Boolean isSSl, String proxyString) {
        LOGGER.info("jsonrpc: method={}, code={}, success={}, url={}, message={}", method, statusCode, success, url, message);
    }

}
