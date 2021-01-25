package com.microee.ethdix.app.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.microee.ethdix.app.listeners.ETHBlockListener;
import com.microee.ethdix.app.props.ETHConfigurationProperties;
import com.microee.ethdix.j3.rpc.JsonRPC;

@Configuration
public class EthdixAppConfig implements ApplicationListener<ApplicationEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(EthdixAppConfig.class);
    
    @Autowired
    private ETHConfigurationProperties ethNetworkProperties;

    @Autowired
    private ETHBlockListener ethBlockListener;
    
    @Bean(name="jsonRPCClientMainnet")
    public JsonRPC jsonRPCClientMainnet() {
        LOGGER.info("ethNetwork-mainnet-nodes={}", String.join(",", ethNetworkProperties.getMainnetNodes()));
        LOGGER.info("ethNetwork-mainnet-wss={}", String.join(",", ethNetworkProperties.getMainnetWss()));
        try {
            return new JsonRPC(ethNetworkProperties.getMainnetNodes(), ethNetworkProperties.getMainnetWss(), ethBlockListener).connect();  
        } catch (Exception e) {
            LOGGER.error("errorMessage={}", e.getMessage(), e);
        }
        return null;
    } 
    
    @Bean(name="jsonRPCClientRopsten")
    public JsonRPC jsonRPCClientRopsten() {
        LOGGER.info("ethNetwork-ropsten-nodes={}", String.join(",", ethNetworkProperties.getRopstenNodes()));
        LOGGER.info("ethNetwork-ropsten-wss={}", String.join(",", ethNetworkProperties.getRopstenWss()));
        try {
            return new JsonRPC(ethNetworkProperties.getRopstenNodes(), ethNetworkProperties.getRopstenWss(), ethBlockListener).connect();  
        } catch (Exception e) {
            LOGGER.error("errorMessage={}", e.getMessage(), e);
        }
        return null;
    } 
    
    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        
    }

}
