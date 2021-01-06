package com.microee.ethdix.app.actions;

import com.microee.ethdix.app.components.GasPriceNow;
import com.microee.ethdix.app.components.Web3JFactory;
import com.microee.ethdix.j3.contract.ERC20ContractQuery;
import com.microee.ethdix.j3.contract.ETHInputEncoder;
import com.microee.ethdix.j3.contract.RemoteCallFunction;
import com.microee.plugin.response.R;
import java.math.BigDecimal;
import java.math.BigInteger;
import org.assertj.core.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// 预估各种gas费
@RestController
@RequestMapping("/estimates")
public class ETHEstimateGasRestful {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ETHEstimateGasRestful.class);
    
    @Autowired
    private Web3JFactory web3JFactory;
    
    // 查询四种 gas 费级别
    @RequestMapping(value = "/now-gasPricing", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<GasPriceNow> gasPricing() {
        GasPriceNow result = GasPriceNow.get();
        if (result != null) {
            result.setRapid((result.getRapid() / Math.pow(10, 9)));
            result.setFast((result.getFast() / Math.pow(10, 9)));
            result.setSlow((result.getSlow() / Math.pow(10, 9)));
            result.setStandard((result.getStandard() / Math.pow(10, 9)));
        }
        return R.ok(result);
    }

    // 根据 gas 费级别计算交易手续费
    @RequestMapping(value = "/eth-transactionFee", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<BigDecimal> transactionFee(
            @RequestParam(value = "gasLevel") String gasLevel,
            @RequestParam(value = "gasLimits", required = false, defaultValue = "21000") Long gasLimits) {
        GasPriceNow result = GasPriceNow.get();
        Double gasPrice = result.gasPrice(gasLevel);
        if (gasPrice == -1.0) {
            return R.failed(R.ILLEGAL, "gasLevel 有误");
        }
        return R.ok(new BigDecimal((gasPrice * gasLimits) / Math.pow(10, 18) + ""));
    }

    // 预估 eth 转帐 gas limits
    @RequestMapping(value = "/estimateGasLimitsForETHTransfer", method = RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<Long> estimateGasLimitsForETHTransfer(
            @RequestParam(value = "ethnode", required = true) String ethnode, // 以太坊节点地址
            @RequestParam(value = "fromAddr", required = false) String fromAddr, // 出帐地址
            @RequestParam(value = "toAddr", required = true) String toAddr, // 转入地址, 如果是usdt转帐请传usdt合约地址, 如果是用户间转帐请传用户地址
            @RequestParam(value = "gasPrice", required = false) Long gasPrice, // 即 rapid OR fast OR standard OR slow 对应的值
            @RequestParam(value = "amount", required = true) Double amount // 转帐数量, 单位是 ether
    ) {
        Assertions.assertThat(ethnode == null || ethnode.isEmpty()).withFailMessage("`ethnode` 必传").isFalse();
        Assertions.assertThat(toAddr).withFailMessage("`toAddr` 必传").isNotBlank();
        // 转帐数量(eth的最小单位), 单位是 wei
        // 1eth = 1000000000000000000
        // 0.035eth = 35000000000000000
        final Double amountDouble = (amount * (Math.pow(10, 18))); // 转帐数量
        return R.ok(web3JFactory.getJsonRpcByEthNode(ethnode).estimateGasLimits(fromAddr, toAddr, gasPrice, amountDouble.longValue()));
    }

    // 当代币余额不足时，会报错, 所以调用接口时确保代币余额大于转出的数量
    // 预估 erc20 代币 转帐 gas 费 
    @RequestMapping(value = "/estimateGasLimitsForTokenTransfer", method = RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<Long> estimateGasLimitsForTokenTransfer(
            @RequestParam(value = "ethnode", required = false) String ethnode, // 以太坊节点地址
            @RequestParam(value = "fromAddr", required = true) String fromAddr, // 出帐地址
            @RequestParam(value = "contractAddr", required = true) String contractAddr, // erc20 合约地址
            @RequestParam(value = "toAddr", required = true) String toAddr, // 转入地址, 账户地址
            @RequestParam(value = "gasPrice", required = true) Long gasPrice, // 即 rapid OR fast OR standard OR slow 对应的值
            @RequestParam(value = "amount", required = true) Double amount // 转帐数量, 单位是 erc20代币单位
    ) {
        Assertions.assertThat(ethnode == null || ethnode.isEmpty()).withFailMessage("`ethnode` 必传").isFalse();
        Assertions.assertThat(toAddr).withFailMessage("`toAddr` 必传").isNotBlank();
        BigInteger tokenDecimal = (BigInteger) RemoteCallFunction.build(new ERC20ContractQuery(contractAddr, web3JFactory.getByEthNode(ethnode)).decimals()).call();
        final Double amountDouble = (amount * (Math.pow(10, tokenDecimal.intValue()))); // 转帐数量(代币的最小单位), 例如: 1usdt = 1000000
        final String inputData = ETHInputEncoder.getInputDataForTokenTransfer(toAddr, amountDouble.longValue());
        return R.ok(web3JFactory.getJsonRpcByEthNode(ethnode).estimateGasLimits(fromAddr, contractAddr, gasPrice, inputData));
    }

    // 预估eth兑换代币手续费
    @RequestMapping(value = "/estimateGasLimitsForETH2TokenSwaper", method = RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<Long> estimateGasLimitsForETH2TokenSwaper(
            @RequestParam(value = "ethnode", required = false) String ethnode, // 以太坊节点地址
            @RequestParam(value = "fromAddr", required = true) String fromAddr, // 出帐地址
            @RequestParam(value = "contractAddr", required = true) String contractAddr, // 代币地址
            @RequestParam(value = "uniswapExchangeAddr", required = true) String uniswapExchangeAddr, // uniswap 兑换合约地址
            @RequestParam(value = "gasPrice", required = true) Long gasPrice, // 即 rapid OR fast OR standard OR slow 对应的值
            @RequestParam(value = "amount", required = true) Double amount // 转帐数量, 单位是 erc20代币单位
    ) {
        Assertions.assertThat(ethnode == null || ethnode.isEmpty()).withFailMessage("`ethnode` 必传").isFalse();
        BigInteger tokenDecimal = (BigInteger) RemoteCallFunction.build(new ERC20ContractQuery(contractAddr, web3JFactory.getByEthNode(ethnode)).decimals()).call();
        final Double amountDouble = (amount * (Math.pow(10, tokenDecimal.intValue()))); // 转帐数量(代币的最小单位), 例如: 1usdt = 1000000
        final String inputData = ETHInputEncoder.getInputDataForTokenTransfer(uniswapExchangeAddr, amountDouble.longValue());
        LOGGER.info("inputData: amount={}, tokenDecimal={}, amountDouble={}, inputData={}", amount, tokenDecimal, amountDouble, inputData);
        return R.ok(web3JFactory.getJsonRpcByEthNode(ethnode).estimateGasLimits(fromAddr, uniswapExchangeAddr, gasPrice, inputData));
    }
    
}
