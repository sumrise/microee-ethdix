package com.microee.ethdix.web.websocket;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class NewBlockWebSocketor {

    private static final Logger LOGGER = LoggerFactory.getLogger(NewBlockWebSocketor.class);
    
    @Autowired(required=false)
    private SimpMessagingTemplate template;
    
    // 发布新块信息
    public void publish(String topic, String message) {
        String _message = new JSONObject(message).toString();
        template.convertAndSend(topic, _message);
        LOGGER.info("websocket公布了一条消息: topic={}, message={}", topic, message);
    }
    
}
