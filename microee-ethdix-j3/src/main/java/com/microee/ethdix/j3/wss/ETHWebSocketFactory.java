package com.microee.ethdix.j3.wss;

import com.microee.ethdix.j3.rpc.JsonRPC.NetworkConfig;
import com.microee.ethdix.oem.eth.enums.ChainId;
import com.microee.plugin.http.assets.HttpClient;
import com.microee.plugin.http.assets.HttpWebsocketListener;
import com.microee.plugin.thread.ThreadPoolConfig;

public class ETHWebSocketFactory {

    private final ChainId chainId;
    private final String ethWsHost;
    private final HttpClient wsHttpClient;
    private final ETHWebsocketMessageHandler ethWebsocketMessageHandler;
    private final ETHWebsocketThreader ethWebsocketThreader;
    private final String webSocketThreadPoolName;
    private final HttpWebsocketListener httpWebsocketListener;
    
    public ETHWebSocketFactory(ChainId chainId, String wss, ETHMessageListener ethMessageListener) {
        this.chainId = chainId;
        this.ethWsHost = wss;
        this.wsHttpClient = HttpClient.create();
        this.ethWebsocketMessageHandler = new ETHWebsocketMessageHandler(chainId, ethMessageListener); 
        this.ethWebsocketThreader = new ETHWebsocketThreader(this);
        this.webSocketThreadPoolName = "ASYN-ETH-WEBSOCKET-LISTENER-POOL-" + chainId.name.toUpperCase();
        this.httpWebsocketListener = new HttpWebsocketListener(this.ethWebsocketMessageHandler, this.webSocketThreadPoolName);
    }
    
    public ETHWebSocketFactory(ChainId chainId, String wss, ThreadPoolConfig webSocketThreadPoolConfig, ETHMessageListener ethMessageListener) {
        this.chainId = chainId;
        this.ethWsHost = wss;
        this.wsHttpClient = HttpClient.create();
        this.ethWebsocketMessageHandler = new ETHWebsocketMessageHandler(chainId, ethMessageListener); 
        this.ethWebsocketThreader = new ETHWebsocketThreader(this);
        this.webSocketThreadPoolName = null;
        this.httpWebsocketListener = new HttpWebsocketListener(this.ethWebsocketMessageHandler, webSocketThreadPoolConfig);
    }
    
    public static ETHWebSocketFactory build(ChainId chainId, String wss, ETHMessageListener ethMessageListener) {
        if (wss == null || wss.isEmpty()) {
            return null;
        }
        return new ETHWebSocketFactory(chainId, wss, ethMessageListener);
    }
    
    public static ETHWebSocketFactory build(ChainId chainId, String wss, ThreadPoolConfig webSocketThreadPoolConfig, ETHMessageListener ethMessageListener) {
        if (wss == null || wss.isEmpty()) {
            return null;
        }
        return new ETHWebSocketFactory(chainId, wss, ethMessageListener);
    }
    
    public static ETHWebSocketFactory build(NetworkConfig networkConfig) {
        if (networkConfig.getWss() == null || networkConfig.getWss().isEmpty()) {
            return null;
        }
        return new ETHWebSocketFactory(networkConfig.getChainId(), networkConfig.getWss(), networkConfig.getEthMessageListener());
    }
    
    public static ETHWebSocketFactory build(NetworkConfig networkConfig, ThreadPoolConfig webSocketThreadPoolConfig) {
        if (networkConfig.getWss() == null || networkConfig.getWss().isEmpty()) {
            return null;
        }
        return new ETHWebSocketFactory(networkConfig.getChainId(), networkConfig.getWss(), webSocketThreadPoolConfig, networkConfig.getEthMessageListener());
    }
    
    public ETHWebSocketFactory createETHStream() {
        this.wsHttpClient.websocket(this.ethWsHost, null, this.httpWebsocketListener);
        return this;
    }

    public void subscribe(String message) {
        this.ethWebsocketMessageHandler.writeMessage(message); 
    }
    
    public ETHWebSocketFactory connect() {
        this.ethWebsocketMessageHandler.setConnectStatus(ConnectStatus.CONNECTING); 
        this.ethWebsocketThreader.start();
        return this;
    }

    public void shutdown() {
        this.ethWebsocketMessageHandler.closeWebsocket();
        this.ethWebsocketThreader.shutdown();
    }

    public ChainId getChainId() {
        return chainId;
    }
    
}
