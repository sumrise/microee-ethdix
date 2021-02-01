package com.microee.ethdix.web.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import com.microee.ethdix.web.listener.ETHDixWebRedisMessageListener;
import com.microee.stacks.redis.support.RedisMessageListenerRegistry;

@Configuration
public class ETHDixWebAppConfig implements ApplicationListener<ApplicationEvent>{

    @Value("${topics-broadcase.eth.new-blocks.redis}")
    public String newBlockMessageBroadcastTopic;
    
    @Autowired
    private RedisMessageListenerRegistry redisMessageListenerRegistry;

    @Autowired
    private ETHDixWebRedisMessageListener ethdixWebRedisMessageListener;

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer() {
        return redisMessageListenerRegistry.add(ethdixWebRedisMessageListener, newBlockMessageBroadcastTopic).get();
    }
    
    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof ApplicationReadyEvent) {
            
        }
    }

}
