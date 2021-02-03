package com.microee.ethdix.app.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.microee.ethdix.app.listeners.ETHBlockListener;
import com.microee.ethdix.app.listeners.JsonRpcHttpClientLogger;
import com.microee.ethdix.app.props.ETHConfigurationProperties;
import com.microee.ethdix.j3.rpc.JsonRPC;
import com.microee.ethdix.j3.rpc.JsonRPC.NetworkConfig;
import com.microee.ethdix.oem.eth.enums.ChainId;

@Configuration
public class EthdixAppConfig implements ApplicationListener<ApplicationEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(EthdixAppConfig.class);
    
    @Autowired
    private ETHConfigurationProperties ethNetworkProperties;

    @Autowired
    private ETHBlockListener ethBlockListener;

    @Autowired
    private JsonRpcHttpClientLogger jsonRpcHttpListener;
    
    @Bean(name="jsonRPCClientMainnet")
    public JsonRPC jsonRPCClientMainnet() {
        return new JsonRPC(getJsonRpcConfig(ChainId.MAINNET)).setHttpClientLoggerListener(jsonRpcHttpListener).connect();
    } 
    
    @Bean(name="jsonRPCClientRopsten")
    public JsonRPC jsonRPCClientRopsten() {
        return new JsonRPC(getJsonRpcConfig(ChainId.ROPSTEN)).setHttpClientLoggerListener(jsonRpcHttpListener).connect();  
    } 
    
    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        
    }
    
    public NetworkConfig getJsonRpcConfig(ChainId chainId) {
        NetworkConfig networkConfig = new NetworkConfig();
        networkConfig.setChainId(chainId);
        networkConfig.setEthMessageListener(ethBlockListener);
        if (chainId.equals(ChainId.ROPSTEN)) {
            LOGGER.info("ethNetwork-ropsten-nodes={}", String.join(",", ethNetworkProperties.getRopstenNodes()));
            LOGGER.info("ethNetwork-ropsten-wss={}", String.join(",", ethNetworkProperties.getRopstenWss()));
            networkConfig.setEthnodes(ethNetworkProperties.getRopstenNodes());
            networkConfig.setWss(ethNetworkProperties.getRopstenWss());
        } else if (chainId.equals(ChainId.MAINNET)) {
            LOGGER.info("ethNetwork-mainnet-nodes={}", String.join(",", ethNetworkProperties.getMainnetNodes()));
            LOGGER.info("ethNetwork-mainnet-wss={}", String.join(",", ethNetworkProperties.getMainnetWss()));
            networkConfig.setEthnodes(ethNetworkProperties.getMainnetNodes());
            networkConfig.setWss(ethNetworkProperties.getMainnetWss());
        }
        return networkConfig;
    }

}
