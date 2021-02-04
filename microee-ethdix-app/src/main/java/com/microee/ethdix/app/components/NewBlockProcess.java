package com.microee.ethdix.app.components;

import java.text.SimpleDateFormat;
import java.util.Date;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.microee.ethdix.app.service.block.ETHBlockService;
import com.microee.ethdix.oem.eth.enums.ChainId;
import com.microee.plugin.response.R;
import com.microee.plugin.response.exception.RestException;
import com.microee.stacks.kafka.support.KafkaStringProducer;
import okhttp3.HttpUrl;

@Component
public class NewBlockProcess {

    private static final Logger logger = LoggerFactory.getLogger(NewBlockProcess.class);
    
    @Value("${topics.eth.new-blocks.kafka}")
    private String newETHBlockTopic;
    
    @Autowired(required=false)
    private KafkaStringProducer kafkaStringProducer;

    @Autowired
    private ETHBlockService ethBlockService;

    public void onProcessNewBlock(ChainId chainId, HttpUrl endpoint, Long blockNumber, Long timestamp) {
        logger.info("新块产生: chainId={}, url={}, newBlockNumber={}, dateTime={}", chainId.name, endpoint.toString(), blockNumber, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(timestamp)));
        try {
            if (ethBlockService.ethGetBlockByNumber(chainId, blockNumber) == null) {
                logger.warn("没查到新块: chainId={}, url={}, newBlockNumber={}", chainId.name, endpoint.toString(), blockNumber);
            }
        } catch (RestException e) {
            if (e.getCode() == R.TIME_OUT) {
                logger.warn("查询超时: chainId={}, url={}, newBlockNumber={}", chainId.name, endpoint.toString(), blockNumber);
            }
        }
        if (kafkaStringProducer != null) {
            kafkaStringProducer.sendMessage(newETHBlockTopic, new JSONObject().put("chainId", chainId.code).put("blockNumber",blockNumber).put("timestamp", timestamp).toString());
        }
    }
    
}
