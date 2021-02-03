package com.microee.ethdix.j3.wss;

import com.microee.ethdix.j3.rpc.JsonRPC.NetworkConfig;
import com.microee.ethdix.oem.eth.enums.ChainId;
import com.microee.plugin.http.assets.HttpClient;
import com.microee.plugin.http.assets.HttpWebsocketListener;

public class ETHWebSocketFactory {

    private final ChainId chainId;
    private final String ethWsHost;
    private final HttpClient wsHttpClient;
    private final ETHWebsocketMessageHandler ethWebsocketMessageHandler;
    private final ETHWebsocketThreader ethWebsocketThreader;
    
    public ETHWebSocketFactory(ChainId chainId, String wss, ETHMessageListener ethMessageListener) {
        this.chainId = chainId;
        this.ethWsHost = wss;
        this.wsHttpClient = HttpClient.create();
        this.ethWebsocketMessageHandler = new ETHWebsocketMessageHandler(chainId, ethMessageListener); 
        this.ethWebsocketThreader = new ETHWebsocketThreader(this);
    }
    
    public static ETHWebSocketFactory build(ChainId chainId, String wss, ETHMessageListener ethMessageListener) {
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
    
    public ETHWebSocketFactory createETHStream() {
        this.wsHttpClient.websocket(this.ethWsHost, null, new HttpWebsocketListener(this.ethWebsocketMessageHandler)); 
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
