package com.microee.ethdix.app.service;

import java.io.IOException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.microee.ethdix.oem.eth.enums.ChainId;
import com.microee.stacks.es.supports.ElasticSearchSaveSupport;

@Service
public class JsonRpcHttpClientLogService {

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonRpcHttpClientLogService.class);
    
    @Autowired(required=false)
    private ElasticSearchSaveSupport saveSupport;
    
    public void save(ChainId chainId, boolean success, int statusCode, long contentLength, String method, String url, String message, Long start, Long speed) {
        JSONObject jsonObject = new JSONObject()
                                    .put("chainId", chainId.name)
                                    .put("url", url).put("method", method).put("code", statusCode)
                                    .put("success", success).put("start", start).put("speed", speed);
        if (saveSupport == null) {
            LOGGER.info("jsonrpc: {}", jsonObject.toString());
            return;
        }
        try {
            saveSupport.save("jsonrpc-httpclient-log", jsonObject);
        } catch (IOException e) {
            LOGGER.error("保存es异常: errorMessage={}", e.getMessage(), e);
        }
    }
    
}
