package com.microee.ethdix.app.listeners;

import java.text.SimpleDateFormat;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.microee.ethdix.j3.wss.ETHMessageListener;
import okhttp3.HttpUrl;

@Component
public class ETHBlockListener implements ETHMessageListener {

    private static final Logger logger = LoggerFactory.getLogger(ETHBlockListener.class);

    @Override
    public void onNewBlock(HttpUrl endpoint, Long blockNumber, Long timestamp) {
        logger.info("新块产生: url={}, newBlockNumber={}, dateTime={}",
                endpoint.toString(), blockNumber, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(timestamp)));
    }

}
