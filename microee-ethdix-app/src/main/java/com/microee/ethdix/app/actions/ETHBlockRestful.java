package com.microee.ethdix.app.actions;

import java.util.List;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.microee.ethdix.app.components.ETHBlockShard;
import com.microee.ethdix.app.components.Web3JFactory;
import com.microee.ethdix.app.service.block.ETHBlockService;
import com.microee.ethdix.app.service.block.ETHReceiptService;
import com.microee.ethdix.j3.rpc.JsonRPC;
import com.microee.ethdix.oem.eth.EthRawBlock;
import com.microee.ethdix.oem.eth.EthRawTransaction;
import com.microee.ethdix.oem.eth.EthTransactionReceipt;
import com.microee.ethdix.oem.eth.enums.ChainId;
import com.microee.plugin.response.R;

// 以太坊区块相关接口
// 接口文档 https://infura.io/docs/ethereum/json-rpc
@RestController
@RequestMapping("/blocks")
public class ETHBlockRestful {

    @Autowired
    private Web3JFactory web3JFactory;

    @Autowired
    private ETHBlockService blockService;

    @Autowired
    private ETHReceiptService txReceiptService;

    @Autowired
    private ETHBlockShard ethBlockShard;

    // ### 查询节点使用量
    @RequestMapping(value = "/used-count", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<Map<String, Map<String, Integer>>> useCount() {
        return R.ok(JsonRPC.UsedCount.get());
    }

    // ### 查询账户信息
    @RequestMapping(value = "/eth-accounts", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<JSONArray> ethAccounts(
            @RequestParam(value = "ethnode", required = false) String ethnode,
            @RequestParam(value = "chainId", required = false, defaultValue = "mainnet") String chainId) {
        Assertions.assertThat(ChainId.get(chainId)).withFailMessage("%s 有误", "chainId").isNotNull();
        return R.ok(new JSONArray(web3JFactory.getJsonRpc(ChainId.get(chainId), ethnode)));
    }

    // ### 查询以太坊节点最新高度
    @RequestMapping(value = "/eth-blockNumber", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<Long> ethBlockNumber(
            @RequestParam(value = "ethnode", required = false) String ethnode,
            @RequestParam(value = "chainId", required = false, defaultValue = "mainnet") String chainId) {
        Assertions.assertThat(ChainId.get(chainId)).withFailMessage("%s 有误", "chainId").isNotNull();
        long lastHeight = web3JFactory.getJsonRpc(ChainId.get(chainId), ethnode).blockNumber();
        return R.ok(lastHeight).message("0x" + Long.toHexString(lastHeight));
    }

    // ### 找出不连续的区块id
    @RequestMapping(value = "/eth-breakBlockNumber", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<List<Long>> ethBreakBlockNumber(
            @RequestParam(value = "start") Long start,
            @RequestParam(value = "end") Long end) {
        String collectionName = ethBlockShard.getCollection(ETHBlockService.COLLECTION_BLOCKS, start);
        return R.ok(blockService.ethBreakBlockNumber(collectionName, start, end)).message(collectionName);
    }

    // ### 根据高度获取区块, 通过以太坊浏览器查看该区块: https://etherscan.io/block/{blockNumber}
    @RequestMapping(value = "/eth-getBlockByNumber", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<EthRawBlock> ethGetBlockByNumber(
            @RequestParam(value = "ethnode", required = false) String ethnode,
            @RequestParam(value = "chainId", required = false, defaultValue = "mainnet") String chainId,
            @RequestParam(value = "blockNumber", required = true) Long blockNumber,
            @RequestParam(value = "decode", required = false, defaultValue = "false") Boolean decode) {
        Assertions.assertThat(ChainId.get(chainId)).withFailMessage("%s 有误", "chainId").isNotNull();
        EthRawBlock currentBlock = blockService.ethGetBlockByNumber(ethnode, ChainId.get(chainId), blockNumber);
        if (decode) {

        }
        return R.ok(currentBlock);
    }

    // ### 根据交易哈希查询交易所在区块编号
    @RequestMapping(value = "/eth-getBlockNumberByTransHash", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<Long> getBlockNumberByTransHash(
            @RequestParam(value = "ethnode", required = false) String ethnode,
            @RequestParam(value = "chainId", required = false, defaultValue = "mainnet") String chainId,
            @RequestParam(value = "transHash", required = true) String transHash) {
        Assertions.assertThat(ChainId.get(chainId)).withFailMessage("%s 有误", "chainId").isNotNull();
        Assertions.assertThat(transHash).withFailMessage("%s 必传", "transHash").isNotBlank();
        return R.ok(this.txReceiptService.getBlockNumberByTransHash(ethnode, ChainId.get(chainId), transHash));
    }

    // ### 根据高度获取区块
    @RequestMapping(value = "/eth-getBlockByHexNumber", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<EthRawBlock> ethGetBlockByHexNumber(
            @RequestParam(value = "ethnode", required = false) String ethnode,
            @RequestParam(value = "chainId", required = false, defaultValue = "mainnet") String chainId,
            @RequestParam(value = "blockHexNumber", required = true) String blockHexNumber) {
        Assertions.assertThat(ChainId.get(chainId)).withFailMessage("%s 有误", "chainId").isNotNull();
        Assertions.assertThat(blockHexNumber).withFailMessage("%s 必传", "blockHexNumber").isNotBlank();
        Assertions.assertThat(blockHexNumber).withFailMessage("%s 格式有误,必须是16进制数", blockHexNumber).startsWith("0x");
        Long blockNumber = Long.parseLong(blockHexNumber.substring(2), 16);
        return R.ok(blockService.ethGetBlockByNumber(ethnode, ChainId.get(chainId), blockNumber)).message("该区块的高度是`" + blockNumber + "`");
    }

    // ### 根据交易哈希获取交易回执
    @RequestMapping(value = "/eth-getTransactionReceipt", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<EthTransactionReceipt> ethGetTransactionReceipt(
            @RequestParam(value = "ethnode", required = false) String ethnode,
            @RequestParam(value = "chainId", required = false, defaultValue = "mainnet") String chainId,
            @RequestParam(value = "blockNumber", required = false) Long blockNumber,
            @RequestParam(value = "transHash", required = true) String transHash) {
        Assertions.assertThat(ChainId.get(chainId)).withFailMessage("%s 有误", "chainId").isNotNull();
        Assertions.assertThat(transHash).withFailMessage("%s 必传", "transHash").isNotBlank();
        EthTransactionReceipt recept = this.txReceiptService.getTransactionReceipt(ethnode, ChainId.get(chainId), blockNumber, transHash);
        if (recept != null && recept.getBlockNumber() != null) {
            return R.ok(recept).message("该交易所在区块`" + Long.parseLong(recept.getBlockNumber().substring(2), 16) + "`");
        }
        return R.ok(recept);
    }

    // 返回-1代表无效的交易哈希或孤块
    // ### 根据交易哈希查询交易确认数
    @RequestMapping(value = "/eth-getTransConfirm", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<Long> ethTransConfirm(
            @RequestParam(value = "ethnode", required = false) String ethnode,
            @RequestParam(value = "chainId", required = false, defaultValue = "mainnet") String chainId,
            @RequestParam(value = "blockNumber", required = false) Long blockNumber,
            @RequestParam(value = "transHash", required = true) String transHash) {
        Assertions.assertThat(ChainId.get(chainId)).withFailMessage("%s 有误", "chainId").isNotNull();
        Assertions.assertThat(transHash).withFailMessage("%s 必传", "transHash").isNotBlank();
        EthTransactionReceipt transReceipt = this.txReceiptService.getTransactionReceipt(ethnode, ChainId.get(chainId), blockNumber, transHash);
        if (transReceipt == null) {
            return R.ok(-1l).message("该交易不存在");
        }
        // 确认是否是孤块, 如果父级别区块能找到说明不是孤块
        // TODO
        // 确认数等于总高度-减去当前区块高度
        Long currentBlockHeight = Long.parseLong(transReceipt.getBlockNumber().substring(2), 16);
        return R.ok(web3JFactory.getJsonRpc(ChainId.get(chainId), ethnode).blockNumber() - currentBlockHeight);
    }

    // 获取交易基本信息
    // signedTransactionData
    @RequestMapping(value = "/eth-getTransactionByHash", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<EthRawTransaction> getTransactionByHash(
            @RequestParam(value = "ethnode", required = false) String ethnode,
            @RequestParam(value = "chainId", required = false, defaultValue = "mainnet") String chainId,
            @RequestParam(value = "transHash", required = true) String transHash // 交易哈希
    ) {
        Assertions.assertThat(ChainId.get(chainId)).withFailMessage("%s 有误", "chainId").isNotNull();
        return R.ok(web3JFactory.getJsonRpc(ChainId.get(chainId), ethnode).getTransactionByHash(transHash));
    }

}
