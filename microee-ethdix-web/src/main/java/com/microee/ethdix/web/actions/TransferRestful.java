package com.microee.ethdix.web.actions;

import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.microee.ethdix.rmi.ETHEstimateGasRMi;
import com.microee.plugin.response.R;

// 转账
@RestController
@RequestMapping("/transfer")
public class TransferRestful {

    @Autowired
    private ETHEstimateGasRMi ethEstimateGasRMi;

    // 根据 gas 费级别计算交易手续费
    @RequestMapping(value = "/eth-transactionFee", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<BigDecimal> transactionFee(
            @RequestParam(value = "gasLevel") String gasLevel,
            @RequestParam(value = "gasLimits", required = false, defaultValue = "21000") Long gasLimits) {
        return ethEstimateGasRMi.transactionFee(gasLevel, gasLimits);
    }

    // 预估 eth 转帐 gas limits
    @RequestMapping(value = "/estimateGasLimitsForETHTransfer", method = RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<Long> estimateGasLimitsForETHTransfer(
            @RequestParam(value = "chainId", required = false, defaultValue = "mainnet") String chainId, 
            @RequestParam(value = "ethnode", required = true) String ethnode, // 以太坊节点地址
            @RequestParam(value = "fromAddr", required = false) String fromAddr, // 出帐地址
            @RequestParam(value = "toAddr", required = true) String toAddr, // 转入地址, 如果是usdt转帐请传usdt合约地址, 如果是用户间转帐请传用户地址
            @RequestParam(value = "gasPrice", required = false) Long gasPrice, // 即 rapid OR fast OR standard OR slow 对应的值
            @RequestParam(value = "amount", required = true) Double amount // 转帐数量, 单位是 ether
    ) {
        return ethEstimateGasRMi.estimateGasLimitsForETHTransfer(chainId, ethnode, fromAddr, toAddr, gasPrice, amount); 
    }

    // 当代币余额不足时，会报错, 所以调用接口时确保代币余额大于转出的数量
    // 预估 erc20 代币 转帐 gas 费 
    @RequestMapping(value = "/estimateGasLimitsForTokenTransfer", method = RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<Long> estimateGasLimitsForTokenTransfer(
            @RequestParam(value = "chainId", required = false, defaultValue = "mainnet") String chainId, 
            @RequestParam(value = "ethnode", required = false) String ethnode, // 以太坊节点地址
            @RequestParam(value = "fromAddr", required = true) String fromAddr, // 出帐地址
            @RequestParam(value = "contractAddr", required = true) String contractAddr, // erc20 合约地址
            @RequestParam(value = "toAddr", required = true) String toAddr, // 转入地址, 账户地址
            @RequestParam(value = "gasPrice", required = true) Long gasPrice, // 即 rapid OR fast OR standard OR slow 对应的值
            @RequestParam(value = "amount", required = true) BigDecimal amount // 转帐数量, 单位是 erc20代币单位
    ) {
        return ethEstimateGasRMi.estimateGasLimitsForTokenTransfer(chainId, ethnode, fromAddr, contractAddr, toAddr, gasPrice, amount);
    }

    // 预估eth兑换代币手续费
    @RequestMapping(value = "/estimateGasLimitsForETH2TokenSwaper", method = RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<Long> estimateGasLimitsForETH2TokenSwaper(
            @RequestParam(value = "chainId", required = false, defaultValue = "mainnet") String chainId, 
            @RequestParam(value = "ethnode", required = false) String ethnode, // 以太坊节点地址
            @RequestParam(value = "fromAddr", required = true) String fromAddr, // 出帐地址
            @RequestParam(value = "contractAddr", required = true) String contractAddr, // 代币地址
            @RequestParam(value = "uniswapExchangeAddr", required = true) String uniswapExchangeAddr, // uniswap 兑换合约地址
            @RequestParam(value = "gasPrice", required = true) Long gasPrice, // 即 rapid OR fast OR standard OR slow 对应的值
            @RequestParam(value = "amount", required = true) BigDecimal amount // 转帐数量, 单位是 erc20代币单位
    ) {
        return ethEstimateGasRMi.estimateGasLimitsForETH2TokenSwaper(chainId, ethnode, fromAddr, contractAddr, uniswapExchangeAddr, gasPrice, amount);
    }
    
}
