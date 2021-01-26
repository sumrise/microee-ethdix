package com.microee.ethdix.j3.wss;

import java.nio.charset.StandardCharsets;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.microee.ethdix.j3.rpc.JsonRPC.TypeOf;
import com.microee.ethdix.oem.eth.EthRawBlock;
import com.microee.plugin.http.assets.HttpAssets;
import com.microee.plugin.http.assets.HttpWebsocketHandler;
import okhttp3.Response;
import okhttp3.WebSocket;
import okio.ByteString;

public class ETHWebsocketMessageHandler implements HttpWebsocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(ETHWebsocketMessageHandler.class);

    private WebSocket webSocket;
    private ConnectStatus connectStatus;
    private final ETHMessageListener ethMessageListener;

    public ETHWebsocketMessageHandler(ETHMessageListener ethMessageListener) {
        this.connectStatus = ConnectStatus.UNKNOW;
        this.ethMessageListener = ethMessageListener;
    }

    public void writeMessage(String message) {
        this.webSocket.send(message);
        logger.info("发送了一条消息: url={}, message={}", webSocket.request().url(), message);
    }
    
    public ConnectStatus getConnectStatus() {
        return connectStatus;
    }

    public void setConnectStatus(ConnectStatus connectStatus) {
        this.connectStatus = connectStatus;
    }

    @Override
    public void onClosedHandler(WebSocket webSocket, int code, String reason) {
        this.connectStatus = ConnectStatus.CLOSED;
        logger.info("websocket连接已关闭: connectStatus={}, url={}, code={}, reason={}", this.connectStatus.code, webSocket.request().url().toString(), code, reason);
    }

    @Override
    public void onFailureHandler(WebSocket webSocket, Throwable t, String responseText) {
        this.connectStatus = ConnectStatus.FAILED;
        logger.info("websocket连接失败: connectStatus={}, url={}", this.connectStatus.code, webSocket.request().url().toString());
    }

    @Override
    public void onOpenHandler(WebSocket webSocket, Response response) {
        this.connectStatus = ConnectStatus.ONLINE;
        this.webSocket = webSocket;
        logger.info("websocket连接成功: connectStatus={}, url={}", this.connectStatus.code, webSocket.request().url().toString());
    }

    @Override
    public void onMessageStringHandler(WebSocket webSocket, String text) {
        if (text.contains("number") && text.contains("parentHash") && text.contains("transactionsRoot")) {
            JSONObject textJSONObject = new JSONObject(text);
            EthRawBlock newBlock = HttpAssets.parseJson(textJSONObject.getJSONObject("params").getJSONObject("result").toString(), new TypeOf<EthRawBlock>().get());
            Long timestamp = Long.parseLong(newBlock.getTimestamp().substring(2), 16) * 1000;
            Long blockNumber = Long.parseLong(newBlock.getNumber().substring(2), 16);
            if (this.ethMessageListener != null) {
                this.ethMessageListener.onNewBlock(webSocket.request().url(), blockNumber, timestamp);
                return;
            }
        }
        logger.info("onMessageStringHandler: connectStatus={}, text={}", this.connectStatus.code, text);
    }

    @Override
    public void onMessageByteStringHandler(WebSocket webSocket, ByteString bytes) {
        logger.info("onMessageByteStringHandler: connectStatus={}, message={}", this.connectStatus.code, new String(bytes.toByteArray(), StandardCharsets.UTF_8));
    }

    @Override
    public void closeWebsocket() {
        this.connectStatus = ConnectStatus.DESTROY;
        logger.info("websocket连接已关闭: connectStatus={}, url={}", this.connectStatus.code, webSocket.request().url().toString());
    }

    @Override
    public void onTimeoutHandler(String url, long start, long end) {
        this.connectStatus = ConnectStatus.TIMEOUT;
        logger.info("websocket连接超时: connectStatus={}, url={}", this.connectStatus.code, url);
    }

}
