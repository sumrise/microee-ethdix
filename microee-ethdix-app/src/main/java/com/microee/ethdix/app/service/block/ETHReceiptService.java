package com.microee.ethdix.app.service.block;

import java.math.BigInteger;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.microee.ethdix.app.components.ETHBlockShard;
import com.microee.ethdix.app.components.Web3JFactory;
import com.microee.ethdix.j3.Constrants;
import com.microee.ethdix.oem.eth.EthTransactionReceipt;
import com.microee.ethdix.oem.eth.enums.ChainId;
import com.microee.stacks.mongodb.support.Mongo;

@Service
public class ETHReceiptService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ETHReceiptService.class);

    public static final String COLLECTION_NAME = "eth_receipts";

    @Autowired
    private ETHBlockShard ethBlockShard;
    
    @Autowired
    private Mongo mongo;

    @Autowired
    private Web3JFactory web3JFactory;
    
    @Autowired
    private ETHTransService ethBlockTransService;

    // 查询并保存交易回执
    public EthTransactionReceipt getTransactionReceipt(String ethnode, ChainId chainId, Long blockNumber, String txHash) {
        EthTransactionReceipt result = null;
        String receiptCollectionName = ethBlockShard.getCollection(COLLECTION_NAME, blockNumber);
        if (blockNumber != null) {
            result = mongo.queryByStringId(receiptCollectionName, txHash, EthTransactionReceipt.class);
        }
        if (result == null) {
            result = web3JFactory.getJsonRpc(chainId, ethnode).getTransactionReceipt(txHash);
            if ((ethnode == null || ethnode.isEmpty()) && result != null) {
                if (result.getBlockNumber() != null) {
                    mongo.save(receiptCollectionName, result, txHash);
                }
            }
        }
        if (blockNumber != null) {
            ethBlockTransService.ethGetTransaction(ethnode, chainId, blockNumber, txHash);
        }
        return result;
    }

    // 根据交易哈希查询交易所在区块编号
    public Long getBlockNumberByTransHash(String ethnode, ChainId chainId, String txHash) {
        EthTransactionReceipt result = web3JFactory.getJsonRpc(chainId, ethnode).getTransactionReceipt(txHash);
        if (result != null) {
            return Long.parseLong(result.getBlockNumber().substring(2), 16);
        }
        return null;
    }

    public void decodeLogByTopic(EthTransactionReceipt transReceipt) {
        String transHash = transReceipt.getTransactionHash();
        List<EthTransactionReceipt.ReceiptLog> receiptReceiptLogs = transReceipt.getLogs(); // 取得交易日志
        if (receiptReceiptLogs == null || receiptReceiptLogs.isEmpty()) {
            LOGGER.warn("当前交易回执没有交易日志,不是以太坊合约交易: transHash={}", transHash);
            return;
        }
        for (EthTransactionReceipt.ReceiptLog receiptLog : receiptReceiptLogs) {
            List<String> topics = receiptLog.getTopics();
            if (topics.isEmpty()) {
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
                switch (topics.size()) {
                    case 3:
                        // erc20
                        tokenFrom = "0x" + topics.get(1).substring(26);
                        tokenTo = "0x" + topics.get(2).substring(26);
                        String tokenValueStr = receiptLog.getData().substring(2);
                        tokenValue = new BigInteger(tokenValueStr, 16);
                        tokenAddress = receiptLog.getAddress();
                        break;
                    case 1:
                        // erc721
                        String data = receiptLog.getData().replace("0x", "");
                        if (data.length() == 192) {
                            tokenFrom = "0x" + data.substring(0, 64).substring(24);
                            tokenTo = "0x" + data.substring(64, 128).substring(24);
                            tokenAddress = receiptLog.getAddress();
                        }
                        break;
                    case 4:
                        // erc721
                        tokenFrom = "0x" + topics.get(1).substring(26);
                        tokenTo = "0x" + topics.get(2).substring(26);
                        tokenAddress = receiptLog.getAddress();
                        break;
                    default:
                        break;
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
