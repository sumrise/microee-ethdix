package com.microee.ethdix.j3.wss;

import com.microee.plugin.thread.NamedThreadFactory;

public class ETHWebsocketThreader implements Runnable {

    private final ETHWebSocketFactory factory;
    private final Thread currentThread;
    
    public static ETHWebsocketThreader create(ETHWebSocketFactory factory) {
        return new ETHWebsocketThreader(factory);
    }
    
    public ETHWebsocketThreader(ETHWebSocketFactory factory) {
        this.factory = factory;
        this.currentThread = new NamedThreadFactory("以太坊websocket线程").newThread(this);
    }
    
    @Override
    public void run() {
        this.factory.createETHStream();
    }
    
    public void start() {
        this.currentThread.start();
    }

    public void shutdown() {
        if (this.currentThread != null) {
            this.currentThread.interrupt();
        }
    }
}
