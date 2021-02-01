package com.microee.ethdix.web.consumers;

import javax.annotation.PostConstruct;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.microee.stacks.kafka.consumer.KafkaSubscribe;
import com.microee.stacks.redis.support.RedisMessage;

@Component
public class NewBlockConsumer {

    @Value("${topics.eth.new-blocks.kafka}")
    private String newETHBlockTopic;
    
    @Autowired
    private KafkaSubscribe kafkaSubscribe;

    @Autowired
    private RedisMessage redisMessage;

    @Value("${topics-broadcase.eth.new-blocks.redis}")
    public String newBlockMessageBroadcastTopic;
    
    @PostConstruct
    public void init() {
        kafkaSubscribe.create(this::newBlockConsumer, newETHBlockTopic).start();
    }
    
    public void newBlockConsumer(ConsumerRecord<String, String> messageRecord) {
        redisMessage.send(newBlockMessageBroadcastTopic, messageRecord.value());
    }
    
}
