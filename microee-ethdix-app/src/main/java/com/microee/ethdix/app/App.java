package com.microee.ethdix.app;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import com.microee.stacks.es.config.ElasticSearchEnabled;
import com.microee.stacks.kafka.config.KafkaEnabled;
import com.microee.stacks.mongodb.config.MongoEnabled;
import com.microee.stacks.starter.MainApp;

@EnableDiscoveryClient
@EnableAutoConfiguration()
@ComponentScan(basePackages = {"com.microee"})
@EnableFeignClients(basePackages = {"com.microee.**.rmi", "com.uniswap.**.rmi"})
@SpringBootApplication
@MongoEnabled()
@KafkaEnabled()
@ElasticSearchEnabled()
public class App extends MainApp {
    public static void main(String[] args) {
        startup(App.class, args);
    }
}
