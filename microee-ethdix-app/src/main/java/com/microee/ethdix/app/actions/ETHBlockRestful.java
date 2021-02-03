package com.microee.ethdix.app.actions;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
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
import com.microee.plugin.commons.RegexUtils;
import com.microee.plugin.response.R;
import com.microee.plugin.response.exception.RestException;
import com.microee.stacks.es.supports.ElasticSearchIndexSupport;

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

    @Autowired
    private ElasticSearchIndexSupport searchIndexSupport;

    // ### 创建索引
    @RequestMapping(value = "/createIndex", method = RequestMethod.POST, consumes=MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<String> createIndex(
            @RequestParam(value = "indexName", required = true) String indexName,
            @RequestParam(value = "aliasName", required = true, defaultValue = "mainnet") String aliasName,
            @RequestParam(value = "numberOfShards", required = false, defaultValue = "3") Integer numberOfShards,
            @RequestParam(value = "numberOfReplicas", required = false, defaultValue = "3") Integer numberOfReplicas,
            @RequestBody Map<String, Object> mappingProperties) throws IOException {
        return R.ok(searchIndexSupport.createIndex(indexName, aliasName, numberOfShards, numberOfReplicas, mappingProperties));
    }

    // ### 删除索引
    @RequestMapping(value = "/deleteIndex", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<Boolean> deleteIndex(
            @RequestParam(value = "indexName", required = true) String indexName) throws IOException {
        return R.ok(searchIndexSupport.deleteIndex(indexName));
    }

    // ### 查询节点使用量
    @RequestMapping(value = "/used-count", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
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
            @RequestParam(value = "chainId", required = false, defaultValue = "mainnet") String chainId,
            @RequestParam(value = "start") Long start,
            @RequestParam(value = "end") Long end) {
        Assertions.assertThat(ChainId.get(chainId)).withFailMessage("%s 有误", "chainId").isNotNull();
        Assertions.assertThat(start).withFailMessage("%s 有误", "start").isNotNull().isGreaterThanOrEqualTo(0l);
        Assertions.assertThat(end).withFailMessage("%s 有误", "end").isNotNull().isGreaterThan(start);
        String collectionName = ethBlockShard.getCollection(ChainId.get(chainId), ETHBlockService.COLLECTION_BLOCKS, start);
        return R.ok(blockService.ethBreakBlockNumber(collectionName, start, end)).message(collectionName);
    }

    // 因为区块编号有索引，所以仅提供通过区块编号查询区块
    // 一个包含0个交易的区块: https://etherscan.io/block/11683188 
    // 一个包含0个交易的区块: https://etherscan.io/block/11684296 
    // ### 根据高度获取区块, 通过以太坊浏览器查看该区块: https://etherscan.io/block/{blockNumber}
    @RequestMapping(value = "/eth-getBlockByNumber", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<EthRawBlock> ethGetBlockByNumber(
            @RequestParam(value = "ethnode", required = false) String ethnode,
            @RequestParam(value = "chainId", required = false, defaultValue = "mainnet") String chainId,
            @RequestParam(value = "blockNumber", required = true) Long blockNumber,
            @RequestParam(value = "fanout", required = false, defaultValue = "false") Boolean fanout, // true: 直接查链, false: 从数据库查
            @RequestParam(value = "decode", required = false, defaultValue = "false") Boolean decode) {
        Assertions.assertThat(ChainId.get(chainId)).withFailMessage("%s 有误", "chainId").isNotNull();
        EthRawBlock currentBlock = blockService.ethGetBlockByNumber(ethnode, ChainId.get(chainId), blockNumber, fanout);
        //if (decode) {
            // TODO
        //}
        if (currentBlock == null) {
            return R.ok(null);
        }
        return R.ok(currentBlock).message("该区块包含" + currentBlock.getTransactions().size() + "笔交易");
    }

    // 一个包含0个交易的区块: https://etherscan.io/block/11683188 
    // 一个包含0个交易的区块: https://etherscan.io/block/11684296 
    // ### 根据交易哈希查询交易所在区块编号
    @RequestMapping(value = "/eth-getBlockNumberByTransHash", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<Long> getBlockNumberByTransHash(
            @RequestParam(value = "ethnode", required = false) String ethnode,
            @RequestParam(value = "chainId", required = false, defaultValue = "mainnet") String chainId,
            @RequestParam(value = "transHash", required = true) String transHash) {
        Assertions.assertThat(ChainId.get(chainId)).withFailMessage("%s 有误", "chainId").isNotNull();
        Assertions.assertThat(transHash).withFailMessage("%s 必传", "transHash").isNotBlank();
        Long blockNumber = this.txReceiptService.getBlockNumberByTransHash(ethnode, ChainId.get(chainId), transHash);
        if (blockNumber == null) {
            return R.ok(-1l).message("该交易无回执,无法查询高度.");
        }
        return R.ok(blockNumber);
    }

    // ### 根据高度获取区块
    @RequestMapping(value = "/eth-getBlockByHexNumber", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<EthRawBlock> ethGetBlockByHexNumber(
            @RequestParam(value = "ethnode", required = false) String ethnode,
            @RequestParam(value = "chainId", required = false, defaultValue = "mainnet") String chainId,
            @RequestParam(value = "blockHexNumber", required = true) String blockHexNumber,
            @RequestParam(value = "fanout", required = false, defaultValue = "false") Boolean fanout // true: 直接查链, false: 从数据库查
    ) {
        Assertions.assertThat(ChainId.get(chainId)).withFailMessage("%s 有误", "chainId").isNotNull();
        Assertions.assertThat(blockHexNumber).withFailMessage("%s 必传", "blockHexNumber").isNotBlank();
        Assertions.assertThat(blockHexNumber).withFailMessage("%s 格式有误,必须是16进制数", blockHexNumber).startsWith("0x");
        Long blockNumber = Long.parseLong(blockHexNumber.substring(2), 16);
        return R.ok(blockService.ethGetBlockByNumber(ethnode, ChainId.get(chainId), blockNumber, fanout)).message("该区块的高度是`" + blockNumber + "`");
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

    // 一个包含0个交易的区块: https://etherscan.io/block/11683188 
    // 一个包含0个交易的区块: https://etherscan.io/block/11684296 
    // 返回-1代表无效的交易哈希或孤块
    // ### 根据交易哈希查询交易确认数
    @RequestMapping(value = "/eth-getTransConfirm", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<Long> ethTransConfirm(
            @RequestParam(value = "ethnode", required = false) String ethnode,
            @RequestParam(value = "chainId", required = false, defaultValue = "mainnet") String chainId,
            @RequestParam(value = "blockNumber", required = false) Long blockNumber,
            @RequestParam(value = "txHash", required = false) String txHash) {
        Assertions.assertThat(ChainId.get(chainId)).withFailMessage("%s 有误", "chainId").isNotNull();
        EthRawTransaction tx = null;
        String _txHash = null;
        if (txHash != null) {
            tx = web3JFactory.getJsonRpc(ChainId.get(chainId), ethnode).getTransactionByHash(txHash);
            if (tx != null) {
                Long findedBlockNumber = Long.parseLong(tx.getBlockNumber().substring(2), 16);
                if (blockNumber != null && blockNumber.compareTo(findedBlockNumber) != 0) {
                    throw new RestException(R.ILLEGAL, "区块编号和交易哈希不匹配:"+ findedBlockNumber +".");
                }
                blockNumber = findedBlockNumber;
                _txHash = tx.getHash();
            }
        }
        if (blockNumber == null) {
            return R.ok(null);
        }
        EthRawBlock block = this.blockService.ethGetBlockByNumber(ethnode, ChainId.get(chainId), blockNumber, false);
        if (this.blockService.ethBlockLonely(ethnode, ChainId.get(chainId), block)) {
            // 孤块
            return R.ok(-1l).message("孤块.");
        }
        // 确认数等于总高度-减去当前区块高度
        return R.ok(web3JFactory.getJsonRpc(ChainId.get(chainId), ethnode).blockNumber() - blockNumber).message(_txHash);
    }

    // 当前区块是否是孤块
    @RequestMapping(value = "/eth-BlockLonely", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<Boolean> ethBlockLonely(
            @RequestParam(value = "ethnode", required = false) String ethnode,
            @RequestParam(value = "chainId", required = false, defaultValue = "mainnet") String chainId,
            @RequestParam(value = "blockNumber", required = false) Long blockNumber) {
        Assertions.assertThat(ChainId.get(chainId)).withFailMessage("%s 有误", "chainId").isNotNull();
        if (blockNumber == -1) {
            throw new RestException(R.ILLEGAL, "区块编号有误");
        }
        EthRawBlock block = this.blockService.ethGetBlockByNumber(ethnode, ChainId.get(chainId), blockNumber, false);
        if (block == null) {
            throw new RestException(R.FAILED, "无效的区块编号");
        }
        return R.ok(this.blockService.ethBlockLonely(ethnode, ChainId.get(chainId), block)).message(block.getHash());
    }

    // 获取交易基本信息
    // 一笔失败的交易: https://etherscan.io/tx/0xcf620d5e212c66b58597ee1c87fcefe53c6e1f72150db8d368df3fb0dd8a0083
    @RequestMapping(value = "/eth-getTransactionByHash", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<EthRawTransaction> getTransactionByHash(
            @RequestParam(value = "ethnode", required = false) String ethnode,
            @RequestParam(value = "chainId", required = false, defaultValue = "mainnet") String chainId,
            @RequestParam(value = "txHash", required = true) String txHash // 交易哈希
    ) {
        Assertions.assertThat(ChainId.get(chainId)).withFailMessage("%s 有误", "chainId").isNotNull();
        Assertions.assertThat(RegexUtils.isHash(txHash, 66)).withFailMessage("%s 有误", "txHash").isNotNull();
        return R.ok(web3JFactory.getJsonRpc(ChainId.get(chainId), ethnode).getTransactionByHash(txHash));
    }

}
