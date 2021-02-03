package com.microee.ethdix.app.listeners;

import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.microee.ethdix.app.components.NewBlockProcess;
import com.microee.ethdix.j3.wss.ETHMessageListener;
import com.microee.ethdix.oem.eth.enums.ChainId;
import com.microee.plugin.thread.ThreadPoolFactoryLow;
import okhttp3.HttpUrl;

@Component
public class ETHBlockListener implements ETHMessageListener, InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(ETHBlockListener.class);
    private static ThreadPoolFactoryLow threadPool = ThreadPoolFactoryLow.create("ethdix-app-新块延迟处理线程池", "ASYN-NEWBLOCK-POOL");
    public static final int DELAY_PROCESS_NEW_BLOCK_TIME_MS = 2000; // 2000 毫秒后处理新块

    private final DelayQueue<DelayedProcessNewBlockItem<ProcessNewBlockItem>> delayProcessNewBlockQueue = new DelayQueue<>();

    @Autowired
    private NewBlockProcess newBlockProcess;

    @Override
    public void onNewBlock(ChainId chainId, HttpUrl endpoint, Long blockNumber, Long timestamp) {
        // 放入延迟队列, 出现新块的时候偶尔会出现在当前节点上触不到的情况, 所以此处延迟2秒处理
        delayProcessNewBlockQueue.put(new DelayedProcessNewBlockItem<>(new ProcessNewBlockItem(chainId, endpoint, blockNumber, timestamp), DELAY_PROCESS_NEW_BLOCK_TIME_MS, TimeUnit.MILLISECONDS));
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.startupDelayProcess();
    }

    private void startupDelayProcess() {
        threadPool.submit(() -> {
            LOGGER.info("启动延迟队列用于处理新块: 延迟时间={} 毫秒", DELAY_PROCESS_NEW_BLOCK_TIME_MS);
            while (true) {
                try {
                    DelayedProcessNewBlockItem<ProcessNewBlockItem> item = delayProcessNewBlockQueue.take();
                    newBlockProcess.onProcessNewBlock(item.item.chainId, item.item.endpoint, item.item.blockNumber, item.item.timestamp);
                } catch (InterruptedException e) {
                    LOGGER.error("errorMessage={}", e.getMessage(), e);
                }
            }
        });
    }

    public static class DelayedProcessNewBlockItem<T> implements Delayed {

        private long time; /* 触发时间 */
        public T item;

        public DelayedProcessNewBlockItem(T item, long time, TimeUnit unit) {
            this.item = item;
            this.time = System.currentTimeMillis() + (time > 0 ? unit.toMillis(time) : 0);
        }

        @Override
        public long getDelay(TimeUnit unit) {
            return time - System.currentTimeMillis();
        }

        @Override
        public int compareTo(Delayed o) {
            @SuppressWarnings("rawtypes")
            DelayedProcessNewBlockItem<?> item = (DelayedProcessNewBlockItem) o;
            long diff = this.time - item.time;
            if (diff <= 0) { // 改成>=会造成问题
                return -1;
            }
            return 1;
        }

    }

    public static class ProcessNewBlockItem {
        
        public final ChainId chainId;
        public final HttpUrl endpoint;
        public final Long blockNumber;
        public final Long timestamp;

        public ProcessNewBlockItem(ChainId chainId, HttpUrl endpoint, Long blockNumber, Long timestamp) {
            super();
            this.chainId = chainId;
            this.endpoint = endpoint;
            this.blockNumber = blockNumber;
            this.timestamp = timestamp;
        }

    }

}
