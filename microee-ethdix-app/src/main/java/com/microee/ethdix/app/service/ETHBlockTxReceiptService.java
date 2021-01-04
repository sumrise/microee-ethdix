package com.microee.ethdix.app.service;

import java.math.BigInteger;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import com.microee.ethdix.app.props.ETHNetworkProperties;
import com.microee.ethdix.j3.Constrants;
import com.microee.ethdix.j3.rpc.JsonRPC;
import com.microee.ethdix.oem.eth.EthTransactionReceipt;
import com.microee.stacks.mongodb.support.Mongo;

@Service
public class ETHBlockTxReceiptService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ETHBlockTxReceiptService.class);
    
    public static final String COLLECTION_NAME = "eth_trans_receipts";

    @Autowired
    private Mongo mongo;

    @Autowired
	@Qualifier("jsonRPCClientMainnet")
    private JsonRPC jsonRPCClient;

    @Autowired
    private ETHTransService ethTransService;
    
    @Autowired
    private ETHNetworkProperties ethNetworkProperties;
    
    // 查询并保存交易回执
    public EthTransactionReceipt getTransactionReceipt(String ethNode, Long blockNumber, String txHash) {
        EthTransactionReceipt result = null;
        if (blockNumber != null) {
            result = mongo.queryByStringId(ethNetworkProperties.getCollectionName(COLLECTION_NAME, blockNumber), txHash, EthTransactionReceipt.class);
        }
        if (result == null) {
            result = jsonRPCClient.getTransactionReceipt(ethNode, txHash);
            if ((ethNode == null || ethNode.isEmpty()) && result != null) {
                if (result.getBlockNumber() != null) {
                    mongo.save(ethNetworkProperties.getCollectionName(COLLECTION_NAME, blockNumber), result, txHash);
                }
            }
        }
        if (blockNumber != null) {
            ethTransService.ethGetTransaction(ethNode, blockNumber, txHash); 
        }
        return result;
    }

    // 根据交易哈希查询交易所在区块编号
    public Long getBlockNumberByTransHash(String ethNode, String txHash) {
        EthTransactionReceipt result = jsonRPCClient.getTransactionReceipt(ethNode, txHash);
        if (result != null) {
            return Long.parseLong(result.getBlockNumber().substring(2), 16);
        }
        return null;
    }
    
    public void decodeLogByTopic(EthTransactionReceipt transReceipt) {
        String transHash = transReceipt.getTransactionHash();
        List<EthTransactionReceipt.ReceiptLog> receiptReceiptLogs = transReceipt.getLogs(); // 取得交易日志
        if (receiptReceiptLogs == null || receiptReceiptLogs.size() == 0) {
            LOGGER.warn("当前交易回执没有交易日志,不是以太坊合约交易: transHash={}", transHash);
            return;
        }
        for (EthTransactionReceipt.ReceiptLog receiptLog : receiptReceiptLogs) {
            List<String> topics = receiptLog.getTopics();
            if (topics.size() == 0) {
                LOGGER.warn("当前交易回执的交易日志没有topic,不是以太坊合约交易: transHash={}", transHash);
                continue;
            }
            String topicHash = topics.get(0);
            String tokenFrom = null;
            String tokenTo = null;
            BigInteger tokenValue = null;
            String tokenAddress = null;
            if (topicHash.equalsIgnoreCase(Constrants.TRANSFER_HASH_MARK)) {
                // 符合 erc20 转账的 event 事件
                if (topics.size() == 3) {
                    // erc20
                    tokenFrom = "0x" + topics.get(1).substring(26);
                    tokenTo = "0x" + topics.get(2).substring(26);
                    String tokenValueStr = receiptLog.getData().substring(2);
                    tokenValue = new BigInteger(tokenValueStr, 16);
                    tokenAddress = receiptLog.getAddress();
                } else if (topics.size() == 1) {
                    // erc721
                    String data = receiptLog.getData().replace("0x", "");
                    if (data.length() == 192) {
                        tokenFrom = "0x" + data.substring(0, 64).substring(24);
                        tokenTo = "0x" + data.substring(64, 128).substring(24);
                        tokenAddress = receiptLog.getAddress();
                    }
                } else if (topics.size() == 4) {
                    // erc721
                    tokenFrom = "0x" + topics.get(1).substring(26);
                    tokenTo = "0x" + topics.get(2).substring(26);
                    tokenAddress = receiptLog.getAddress();
                }
            } else if (topicHash.equalsIgnoreCase(Constrants.ERC1155_TRANSFER_MARK)
                    && topics.size() == 4) {
                // erc1155
                tokenFrom = "0x" + topics.get(2).substring(26);
                tokenTo = "0x" + topics.get(3).substring(26);
                String data = receiptLog.getData().replace("0x", "");
                tokenValue = new BigInteger(data.substring(64, 128), 16);
                tokenAddress = receiptLog.getAddress();
            } else {
            }
            if (tokenFrom != null && !tokenFrom.isEmpty()) {
                LOGGER.info(
                        "当前交易是以太坊合约交易: topicHash={}, tokenFrom={}, tokenTo={}, tokenValue={}, tokenAddress={}",
                        topicHash, tokenFrom, tokenTo, tokenValue, tokenAddress);
            } else {
                LOGGER.warn("当前交易仅仅是以太坊转账交易: transHash={}", transHash);
            }
        }
    }
    
}
