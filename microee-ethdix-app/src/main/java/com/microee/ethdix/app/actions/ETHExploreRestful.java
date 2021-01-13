package com.microee.ethdix.app.actions;

import java.math.BigInteger;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.microee.ethdix.app.service.block.ETHTransService;
import com.microee.ethdix.app.service.block.ETHReceiptService;
import com.microee.ethdix.j3.Constrants;
import com.microee.ethdix.oem.eth.EthRawTransaction;
import com.microee.ethdix.oem.eth.EthTransactionReceipt;
import com.microee.ethdix.oem.eth.enums.ChainId;
import com.microee.ethdix.oem.eth.enums.ETHContractType;
import com.microee.plugin.response.R;

@RestController
@RequestMapping("/explore")
public class ETHExploreRestful {

    private static final Logger LOGGER = LoggerFactory.getLogger(ETHBlockRestful.class);

    @Autowired
    private ETHReceiptService txReceiptService;

    @Autowired
    private ETHTransService ethBlockTransService;

    // ### 获取交易基本信息
    @RequestMapping(value = "/eth-getTransaction", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<EthRawTransaction> ethGetTransaction(
            @RequestParam(value = "ethnode", required = false) String ethnode, // 以太坊节点地址
            @RequestParam(value = "chainId", required = false, defaultValue = "mainnet") String chainId, // 网络类型: 主网或测试网
            @RequestParam("blockNumber") Long blockNumber, 
            @RequestParam("transHash") String transHash) {
        Assertions.assertThat(ChainId.get(chainId)).withFailMessage("%s 有误", "chainId").isNotNull();
        Assertions.assertThat(transHash).withFailMessage("%s 必传", "transHash").isNotBlank();
        EthRawTransaction trans = ethBlockTransService.ethGetTransaction(ethnode, ChainId.get(chainId), blockNumber, transHash); 
        if (trans == null) {
            return R.ok(null);
        }
        if (trans.getBlockNumber() != null) {
            return R.ok(trans).message("该交易所在区块`" + Long.parseLong(trans.getBlockNumber().substring(2), 16) + "`");
        }
        return R.ok(trans);
    }
    
    // 通过区块浏览器查看该交易的信息
    // https://etherscan.io/tx/{transHash}
    // https://etherscan.io/address/{fromAddress OR toAddress}
    // ### 交易解码
    @RequestMapping(value = "/eth-getTransDecoder", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<String> getTransDecoder(
            @RequestParam(value = "ethnode", required = false) String ethnode,
            @RequestParam(value = "chainId", required = false, defaultValue = "mainnet") String chainId,
            @RequestParam("blockNumber") Long blockNumber, 
            @RequestParam("transHash") String transHash) {
        Assertions.assertThat(ChainId.get(chainId)).withFailMessage("%s 有误", "chainId").isNotNull();
        Assertions.assertThat(transHash).withFailMessage("%s 必传", "transHash").isNotBlank();
        EthTransactionReceipt transReceipt = this.txReceiptService.getTransactionReceipt(ethnode, ChainId.get(chainId), blockNumber, transHash); // 取得交易回执
        List<EthTransactionReceipt.ReceiptLog> receiptReceiptLogs = transReceipt.getLogs(); // 取得交易日志 
        if (receiptReceiptLogs == null || receiptReceiptLogs.isEmpty()) {
            LOGGER.warn("当前交易回执没有交易日志: trans-receipt-hash={}", transHash);
            return R.ok(null);
        }
        ETHContractType contractType = ETHContractType.NAN;
        for (EthTransactionReceipt.ReceiptLog receiptLog : receiptReceiptLogs) {
            List<String> topics = receiptLog.getTopics();
            if (topics.isEmpty()) {
                LOGGER.warn("当前交易日志没有topic: trans-receipt-hash={}", transHash);
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
                        contractType = ETHContractType.ERC20;
                        break;
                    case 1:
                        // erc721
                        String data = receiptLog.getData().replace("0x", "");
                        if (data.length() == 192) {
                            tokenFrom = "0x" + data.substring(0, 64).substring(24);
                            tokenTo = "0x" + data.substring(64, 128).substring(24);
                            tokenAddress = receiptLog.getAddress();
                        }   contractType = ETHContractType.ERC721;
                        break;
                    case 4:
                        // erc721
                        tokenFrom = "0x" + topics.get(1).substring(26);
                        tokenTo = "0x" + topics.get(2).substring(26);
                        tokenAddress = receiptLog.getAddress();
                        contractType = ETHContractType.ERC721;
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
                contractType = ETHContractType.ERC1155;
            } else {
                LOGGER.warn("以太坊交易: topicHash={}", topicHash);
            }
            if (tokenFrom != null && !tokenFrom.isEmpty()) {
                LOGGER.info(
                        "当前区块的交易信息: topicHash={}, tokenFrom={}, tokenTo={}, tokenValue={}, tokenAddress={}",
                        topicHash, tokenFrom, tokenTo, tokenValue, tokenAddress);
            }
        }
        return R.ok(contractType.name).message(contractType.desc);
    }
    
    // 查询指定地址的转入记录
    @RequestMapping(value = "/eth-queryTransferTo", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<List<EthRawTransaction>> queryTransferTo(@RequestParam(value = "toAddress", required = false) String hashAddress) {
        return R.ok(ethBlockTransService.queryTransferTo(hashAddress));
    }
    
    // 查询指定地址的转出记录
    @RequestMapping(value = "/eth-queryTransferFrom", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<List<EthRawTransaction>> queryTransferFrom(@RequestParam(value = "fromAddress", required = false) String hashAddress) {
        return R.ok(ethBlockTransService.queryTransferFrom(hashAddress)); 
    }

}
