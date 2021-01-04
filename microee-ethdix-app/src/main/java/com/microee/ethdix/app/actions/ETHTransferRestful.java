package com.microee.ethdix.app.actions;

import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.web3j.crypto.RawTransaction;
import com.microee.ethdix.app.components.ETHInputEncoderComponent;
import com.microee.ethdix.app.components.Web3JFactory;
import com.microee.ethdix.j3.contract.ERC20ContractQuery;
import com.microee.ethdix.j3.contract.RemoteCallFunction;
import com.microee.ethdix.j3.rpc.JsonRPC;
import com.microee.ethdix.oem.eth.EthRawTransaction;
import com.microee.plugin.response.R;

// 以太坊构建转帐交易相关借口
@RestController
@RequestMapping("/transfer")
public class ETHTransferRestful {

    @Autowired
    private Web3JFactory web3JFactory;

    @Autowired
    private ETHInputEncoderComponent ethInputEncoder;

    // 根据网络类型随机返回一个节点地址
    @RequestMapping(value = "/getETHNode", method = RequestMethod.GET, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<String> getETHNode(
            @RequestParam(value = "network", required = false, defaultValue = "mainnet") String network // 网络类型: 主网或测试网
    ) {
        Assertions.assertThat(network).withFailMessage("`network` 必传").isNotBlank();
        return R.ok(web3JFactory.getEthNode(network));
    }

    // 返回链id
    @RequestMapping(value = "/getChainId", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<Long> getChainId(
            @RequestParam(value = "ethnode", required = true) String ethnode // 以太坊节点地址
    ) {
        Assertions.assertThat(ethnode).withFailMessage("`ethnode` 必传").isNotBlank();
        return R.ok(new JsonRPC().getChainId(ethnode));
    }

    // 查询当前账户地址在传入的节点上发起的交易数量
    @RequestMapping(value = "/getTransactionCount", method = RequestMethod.GET, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<Long> getTransactionCount(
            @RequestParam(value = "ethnode", required = true) String ethnode, // 以太坊节点地址
            @RequestParam(value = "accountAddress", required = true) String accountAddress // 转入地址
    ) {
        Assertions.assertThat(ethnode).withFailMessage("`ethnode` 必传").isNotBlank();
        Assertions.assertThat(accountAddress).withFailMessage("`accountAddress` 必传").isNotBlank();
        return R.ok(new JsonRPC().getTransactionCount(ethnode, accountAddress));
    }

    // 预估 eth 转帐 gas limits
    @RequestMapping(value = "/estimateGasLimitsForETHTransfer", method = RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<Long> estimateGasLimitsForETHTransfer(
            @RequestParam(value = "ethnode", required = false) String ethnode, // 以太坊节点地址
            @RequestParam(value = "fromAddress", required = false) String fromAddress, // 出帐地址
            @RequestParam(value = "toAddress", required = true) String toAddress, // 转入地址, 如果是usdt转帐请传usdt合约地址, 如果是用户间转帐请传用户地址
            @RequestParam(value = "gasPrice", required = false) Long gasPrice, // 即 rapid OR fast OR standard OR slow 对应的值
            @RequestParam(value = "amount", required = false) Double amount // 转帐数量, 单位是 ether
    ) {
        Assertions.assertThat(ethnode == null || ethnode.isEmpty()).withFailMessage("`ethnode` 必传").isFalse();
        Assertions.assertThat(toAddress).withFailMessage("`toAddress` 必传").isNotBlank();
        // 转帐数量(eth的最小单位), 单位是 wei
        // 1eth = 1000000000000000000
        // 0.035eth = 35000000000000000
        final Double amountDouble = (amount * (Math.pow(10, 18))); // 转帐数量
        return R.ok(new JsonRPC().estimateGasLimits(ethnode, fromAddress, toAddress, gasPrice, amountDouble.longValue()));
    }

    // 签名交易: 构建ETH转帐交易签名数据, 返回签名后的数据
    // signedTransactionData
    @RequestMapping(value = "/signETHTransferTransaction", method = RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<String> signETHTransferTransaction(
            @RequestParam(value = "ethnode", required = false) String ethnode, // 以太坊节点地址
            @RequestParam(value = "toAddress", required = true) String toAddress, // 转入地址
            @RequestParam(value = "gasPrice", required = true) Long gasPrice, // 即 rapid OR fast OR standard OR slow 对应的值
            @RequestParam(value = "gasLimit", required = false, defaultValue = "21000") Long gasLimit,
            @RequestParam(value = "amount", required = true) Double amount, // 转帐数量, 单位是 ether
            @RequestParam(value = "fromAddress", required = true) String fromAddress, // 出帐地址
            @RequestParam(value = "privateKey", required = true) String privateKey) { // 地址私钥
        Assertions.assertThat(ethnode == null || ethnode.isEmpty()).withFailMessage("`ethnode` 必传").isFalse();
        Assertions.assertThat(toAddress).withFailMessage("`toAddress` 必传").isNotBlank();
        Assertions.assertThat(gasPrice).withFailMessage("`gasPrice`必须大于0").isGreaterThan(0l);
        Assertions.assertThat(gasLimit).withFailMessage("`gasLimit`必须大于0").isGreaterThan(0l);
        Assertions.assertThat(amount).withFailMessage("`amount`必须大于0").isGreaterThan(0.0d);
        Assertions.assertThat(privateKey).withFailMessage("`privateKey`必传或格式不对").isNotBlank().hasSize(64);
        Assertions.assertThat(toAddress.equals(fromAddress)).withFailMessage("`from` and `to` 必须不相等").isFalse();
        // 转帐数量(eth的最小单位), 单位是 wei
        // 1eth = 1000000000000000000
        // 0.035eth = 35000000000000000
        final Double amountDouble = (amount * (Math.pow(10, 18))); // 转帐数量
        return R.ok(new JsonRPC().signETHTransaction(ethnode, fromAddress, toAddress, gasPrice, gasLimit, amountDouble.longValue(), privateKey));
    }

    // 预估 erc20 代币 转帐 gas 费 
    @RequestMapping(value = "/estimateGasLimitsForTokenTransfer", method = RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<Long> estimateGasLimitsForTokenTransfer(
            @RequestParam(value = "ethnode", required = false) String ethnode, // 以太坊节点地址
            @RequestParam(value = "contractAddress", required = true) String contractAddress, // erc20 合约地址
            @RequestParam(value = "toAddress", required = true) String toAddress, // 转入地址, 账户地址
            @RequestParam(value = "gasPrice", required = true) Long gasPrice, // 即 rapid OR fast OR standard OR slow 对应的值
            @RequestParam(value = "amount", required = true) Double amount, // 转帐数量, 单位是 erc20代币单位
            @RequestParam(value = "fromAddress", required = true) String fromAddress // 出帐地址
    ) {
        Assertions.assertThat(ethnode == null || ethnode.isEmpty()).withFailMessage("`ethnode` 必传").isFalse();
        Assertions.assertThat(toAddress).withFailMessage("`toAddress` 必传").isNotBlank();
        BigInteger tokenDecimal = (BigInteger) new RemoteCallFunction<>(new ERC20ContractQuery(contractAddress, web3JFactory.get(null, ethnode)).decimals()).call();
        final Double amountDouble = (amount * (Math.pow(10, tokenDecimal.intValue()))); // 转帐数量(代币的最小单位), 例如: 1usdt = 1000000
        final String inputData = ethInputEncoder.getTransferInputData(toAddress, amountDouble.longValue());
        return R.ok(new JsonRPC().estimateGasLimits(ethnode, fromAddress, contractAddress, gasPrice, inputData));
    }
    
    // 签名交易: 构建 erc20 交易签名, 返回签名后的数据
    // signedTransactionData
    @RequestMapping(value = "/signTokenTransferTransaction", method = RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<String> signTokenTransferTransaction(
            @RequestParam(value = "ethnode", required = false) String ethnode, // 以太坊节点地址
            @RequestParam(value = "contractAddress", required = true) String contractAddress, // erc20 合约地址
            @RequestParam(value = "toAddress", required = true) String toAddress, // 转入地址, 账户地址
            @RequestParam(value = "gasPrice", required = true) Long gasPrice, // 即 rapid OR fast OR standard OR slow 对应的值
            @RequestParam(value = "gasLimit", required = false, defaultValue = "21000") Long gasLimit,
            @RequestParam(value = "amount", required = true) Double amount, // 转帐数量, 单位是 erc20代币单位
            @RequestParam(value = "fromAddress", required = true) String fromAddress, // 出帐地址
            @RequestParam(value = "privateKey", required = true) String privateKey) throws Exception { // 地址私钥
        Assertions.assertThat(ethnode == null || ethnode.isEmpty()).withFailMessage("`ethnode` 必传").isFalse();
        Assertions.assertThat(contractAddress).withFailMessage("`contractAddress` 必传").isNotBlank();
        Assertions.assertThat(toAddress).withFailMessage("`toAddress` 必传").isNotBlank();
        Assertions.assertThat(gasPrice).withFailMessage("`gasPrice`必须大于0").isGreaterThan(0l);
        Assertions.assertThat(gasLimit).withFailMessage("`gasLimit`必须大于0").isGreaterThan(0l);
        Assertions.assertThat(amount).withFailMessage("`amount`必须大于0").isGreaterThan(0.0d);
        Assertions.assertThat(privateKey).withFailMessage("`privateKey`必传或格式不对").isNotBlank().hasSize(64);
        Assertions.assertThat(toAddress.equals(fromAddress)).withFailMessage("`from` and `to` 必须不相等").isFalse();
        BigInteger tokenDecimal = (BigInteger) new RemoteCallFunction<>(new ERC20ContractQuery(contractAddress, web3JFactory.get(null, ethnode)).decimals()).call();
        final Double amountDouble = (amount * (Math.pow(10, tokenDecimal.intValue()))); // 转帐数量(代币的最小单位), 例如: 1usdt = 1000000
        final String inputData = ethInputEncoder.getTransferInputData(toAddress, amountDouble.longValue());
        return R.ok(new JsonRPC().signTokenTransaction(ethnode, fromAddress, contractAddress, gasPrice, gasLimit, privateKey, inputData));
    }

    // 根据签名后的数据获取交易哈希
    @RequestMapping(value = "/getTransactionHash", method = RequestMethod.POST, consumes = MediaType.TEXT_PLAIN_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<String> getTransactionHash(
            @RequestBody String signedTransactionData // 签名后的交易哈希
    ) {
        return R.ok(JsonRPC.getTransactionHash(signedTransactionData));
    }

    // 解码交易数据
    @RequestMapping(value = "/decodeSignedTransactionData", method = RequestMethod.POST, consumes = MediaType.TEXT_PLAIN_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<Map<String, Object>> decodeSignedTransactionData(
            @RequestBody String signedTransactionData // 签名后的交易哈希
    ) {
        RawTransaction rawTran = JsonRPC.decodeTransactionHash(signedTransactionData);
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("nonce", rawTran.getNonce());
        map.put("gasPrice", rawTran.getGasPrice());
        map.put("gasLimit", rawTran.getGasLimit());
        map.put("to", rawTran.getTo());
        map.put("value", rawTran.getValue());
        map.put("data", rawTran.getData());
        return R.ok(map);
    }

    // 发送交易
    // signedTransactionData
    @RequestMapping(value = "/sendTransaction", method = RequestMethod.POST, consumes = MediaType.TEXT_PLAIN_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<String> sendTransaction(
            @RequestParam(value = "ethnode", required = false) String ethnode, // 以太坊节点地址
            @RequestBody String signedTransactionData) {
        Assertions.assertThat(ethnode == null || ethnode.isEmpty()).withFailMessage("`ethnode` 必传").isFalse();
        return R.ok(new JsonRPC().sendRawTransaction(ethnode, signedTransactionData));
    }

    // 取消交易
    public R<Boolean> cancelTransaction() {
        return R.ok(false);
    }

    // 获取交易基本信息
    // signedTransactionData
    @RequestMapping(value = "/getTransactionByHash", method = RequestMethod.POST, consumes = MediaType.TEXT_PLAIN_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<EthRawTransaction> getTransactionByHash(
            @RequestParam(value = "ethnode", required = false) String ethnode, // 以太坊节点地址
            @RequestBody String transHash // 交易哈希
    ) {
        Assertions.assertThat(ethnode == null || ethnode.isEmpty()).withFailMessage("`ethnode` 必传").isFalse();
        return R.ok(new JsonRPC().getTransactionByHash(ethnode, transHash));
    }

}
