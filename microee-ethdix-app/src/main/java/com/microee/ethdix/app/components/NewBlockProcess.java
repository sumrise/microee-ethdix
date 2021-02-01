package com.microee.ethdix.app.components;

import java.text.SimpleDateFormat;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.microee.ethdix.oem.eth.enums.ChainId;
import okhttp3.HttpUrl;

@Component
public class NewBlockProcess {

    private static final Logger logger = LoggerFactory.getLogger(NewBlockProcess.class);
    
    @Value("${topics.eth.new-blocks.kafka}")
    private String newETHBlockTopic;
    
//    @Autowired(required=false)
//    private KafkaStringProducer kafkaStringProducer;

    //    @Autowired
    //    private ETHBlockService ethBlockService;

    public void onProcessNewBlock(ChainId chainId, HttpUrl endpoint, Long blockNumber, Long timestamp) {
        logger.info("新块产生: chainId={}, url={}, newBlockNumber={}, dateTime={}",
                chainId.name, endpoint.toString(), blockNumber, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(timestamp)));
        // EthRawBlock newBlock = ethBlockService.ethGetBlockByNumber(null, ChainId.MAINNET, blockNumber, true);
        //kafkaStringProducer.sendMessage(newETHBlockTopic, String.valueOf(blockNumber));
    }
    
}
