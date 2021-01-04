package com.microee.ethdix.j3.wss;

import com.microee.plugin.http.assets.HttpClient;
import com.microee.plugin.http.assets.HttpWebsocketListener;

public class ETHWebSocketFactory {

    private final String ethWsHost;
    private final HttpClient wsHttpClient;
    private final ETHWebsocketMessageHandler ethWebsocketMessageHandler;
    private final ETHWebsocketThreader ethWebsocketThreader;
    
    public ETHWebSocketFactory(String wss, ETHMessageListener ethMessageListener) {
        this.ethWsHost = wss;
        this.wsHttpClient = HttpClient.create();
        this.ethWebsocketMessageHandler = new ETHWebsocketMessageHandler(ethMessageListener); 
        this.ethWebsocketThreader = new ETHWebsocketThreader(this);
    }
    
    public static ETHWebSocketFactory build(String wss, ETHMessageListener ethMessageListener) {
        if (wss == null || wss.isEmpty()) {
            return null;
        }
        return new ETHWebSocketFactory(wss, ethMessageListener);
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
    
}
