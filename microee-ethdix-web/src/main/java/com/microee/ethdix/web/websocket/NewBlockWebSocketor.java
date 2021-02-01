package com.microee.ethdix.web.websocket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import com.microee.stacks.ws.WebSocketer;

@Component
public class NewBlockWebSocketor {

    @Autowired
    @Qualifier("newBlockWebSocketor")
    private WebSocketer<String> webSocketer;
    
    @Bean(name = "newBlockWebSocketor")
    public WebSocketer<String> newBlockWebSocketor() {
        return new WebSocketer<>();
    }
    
    // 发布新块信息
    public void publish(String topic, String message) {
        webSocketer.broadcasts(topic, message, true);
    }
    
}
