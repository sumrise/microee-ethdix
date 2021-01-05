package com.microee.ethdix.app.actions;

import com.microee.ethdix.app.components.ETHInputEncoderComponent;
import com.microee.ethdix.app.components.Web3JFactory;
import com.microee.ethdix.j3.contract.ERC20ContractQuery;
import com.microee.ethdix.j3.contract.RemoteCallFunction;
import com.microee.ethdix.j3.rpc.JsonRPC;
import com.microee.plugin.response.R;
import java.math.BigInteger;
import org.assertj.core.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// 预估各种gas费
@RestController
@RequestMapping("/estimate")
public class ETHEstimateGasRestful {

    @Autowired
    private Web3JFactory web3JFactory;

    @Autowired
    private ETHInputEncoderComponent ethInputEncoder;

    // 预估 eth 转帐 gas limits
    @RequestMapping(value = "/estimateGasLimitsForETHTransfer", method = RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<Long> estimateGasLimitsForETHTransfer(
            @RequestParam(value = "ethnode", required = true) String ethnode, // 以太坊节点地址
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
        final Double amountDouble = amount == null ? null : (amount * (Math.pow(10, 18))); // 转帐数量
        return R.ok(web3JFactory.getJsonRpcByEthNode(ethnode).estimateGasLimits(fromAddress, toAddress, gasPrice, amountDouble == null ? null : amountDouble.longValue()));
    }

    // 预估 erc20 代币 转帐 gas 费 
    @RequestMapping(value = "/estimateGasLimitsForTokenTransfer", method = RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<Long> estimateGasLimitsForTokenTransfer(
            @RequestParam(value = "ethnode", required = true) String ethnode, // 以太坊节点地址
            @RequestParam(value = "contractAddress", required = true) String contractAddress, // erc20 合约地址
            @RequestParam(value = "toAddress", required = true) String toAddress, // 转入地址, 账户地址
            @RequestParam(value = "gasPrice", required = false) Long gasPrice, // 即 rapid OR fast OR standard OR slow 对应的值
            @RequestParam(value = "amount", required = false) Double amount, // 转帐数量, 单位是 erc20代币单位
            @RequestParam(value = "fromAddress", required = false) String fromAddress // 出帐地址
    ) {
        Assertions.assertThat(ethnode == null || ethnode.isEmpty()).withFailMessage("`ethnode` 必传").isFalse();
        Assertions.assertThat(toAddress).withFailMessage("`toAddress` 必传").isNotBlank();
        BigInteger tokenDecimal = (BigInteger) new RemoteCallFunction<>(new ERC20ContractQuery(contractAddress, web3JFactory.get(null, ethnode)).decimals()).call();
        final Double amountDouble = amount == null ? null : (amount * (Math.pow(10, tokenDecimal.intValue()))); // 转帐数量(代币的最小单位), 例如: 1usdt = 1000000
        final String inputData = ethInputEncoder.getTransferInputData(toAddress, amountDouble == null ? null : amountDouble.longValue());
        return R.ok(web3JFactory.getJsonRpcByEthNode(ethnode).estimateGasLimits(fromAddress, contractAddress, gasPrice, inputData));
    }

}
