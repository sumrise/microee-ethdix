package com.microee.ethdix.web.listener;

import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;
import com.microee.ethdix.web.websocket.NewBlockWebSocketor;

@Component
public class ETHDixWebRedisMessageListener implements MessageListener {

    @Value("${topics-broadcase.eth.new-blocks.redis}")
    public String newBlockMessageBroadcastTopic;
    
    private static final String _WEBSOCKET_NEWBLOCK_TOPIC = "/topic/new-block";
    
    @Autowired
    private NewBlockWebSocketor newBlockWebSocketor;
    
    @Override
    public void onMessage(Message message, byte[] pattern) {
        String _topic = new String(message.getChannel()); 
        String _message = new String(message.getBody(), StandardCharsets.UTF_8); 
        if (_topic.equals(newBlockMessageBroadcastTopic)) {
            newBlockWebSocketor.publish(_WEBSOCKET_NEWBLOCK_TOPIC, _message);
            return;
        }
    }
    
}
