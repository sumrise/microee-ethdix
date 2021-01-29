package com.microee.ethdix.web.actions;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.microee.ethdix.oem.eth.entity.GasPriceNow;
import com.microee.ethdix.oem.eth.entity.Token;
import com.microee.ethdix.rmi.ETHBalanceRMi;
import com.microee.ethdix.rmi.ETHEstimateGasRMi;
import com.microee.ethdix.rmi.WalletRMi;
import com.microee.ethdix.rmi.univ2.ETHUniSwapV2RMi;
import com.microee.plugin.response.R;

@RestController
@RequestMapping("/")
public class DefaultRestful {

    @Autowired
    private ETHBalanceRMi ethBalanceRMi;

    @Autowired
    private ETHEstimateGasRMi ethEstimateGasRMi;

    @Autowired
    private WalletRMi walletRMi;

    @Autowired
    private ETHUniSwapV2RMi univ2RMi;
    
//    @Autowired
//    private ETHUniSwapV2SwapRMi univ2SwapRMi;
//
//    @Autowired
//    private ETHUniSwapV2SDKRMi univ2SDKRMi;
    
    // ### Balance
    // 查询余额
    @RequestMapping(value = "/query", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<Double> balanceOf(
            @RequestHeader(value = "username", required = false) String username,
            @RequestHeader(value = "password", required = false) String password,
            @RequestParam(value = "ethnode", required = false) String ethnode, // 以太坊节点地址
            @RequestParam(value = "chainId", required = false, defaultValue = "mainnet") String chainId, // 网络类型: 主网或测试网
            @RequestParam(value = "currency") String currency, // 币种
            @RequestParam(value = "accountAddress") String accountAddress) throws Exception {
        return ethBalanceRMi.balanceOf(username, password, ethnode, chainId, currency, accountAddress);
    }

    // #### Estimate 
    // 查询四种 gas 费级别
    @RequestMapping(value = "/now-gasPricing", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<GasPriceNow> gasPricing() {
        return ethEstimateGasRMi.gasPricing();
    }

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
            @RequestParam(value = "ethnode", required = true) String ethnode, // 以太坊节点地址
            @RequestParam(value = "fromAddr", required = false) String fromAddr, // 出帐地址
            @RequestParam(value = "toAddr", required = true) String toAddr, // 转入地址, 如果是usdt转帐请传usdt合约地址, 如果是用户间转帐请传用户地址
            @RequestParam(value = "gasPrice", required = false) Long gasPrice, // 即 rapid OR fast OR standard OR slow 对应的值
            @RequestParam(value = "amount", required = true) Double amount // 转帐数量, 单位是 ether
    ) {
        return ethEstimateGasRMi.estimateGasLimitsForETHTransfer(ethnode, fromAddr, toAddr, gasPrice, amount);
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
            @RequestParam(value = "amount", required = true) BigDecimal amount // 转帐数量, 单位是 erc20代币单位
    ) {
        return ethEstimateGasRMi.estimateGasLimitsForTokenTransfer(ethnode, fromAddr, contractAddr, toAddr, gasPrice, amount);
    }

    // 预估eth兑换代币手续费
    @RequestMapping(value = "/estimateGasLimitsForETH2TokenSwaper", method = RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<Long> estimateGasLimitsForETH2TokenSwaper(
            @RequestParam(value = "ethnode", required = false) String ethnode, // 以太坊节点地址
            @RequestParam(value = "fromAddr", required = true) String fromAddr, // 出帐地址
            @RequestParam(value = "contractAddr", required = true) String contractAddr, // 代币地址
            @RequestParam(value = "uniswapExchangeAddr", required = true) String uniswapExchangeAddr, // uniswap 兑换合约地址
            @RequestParam(value = "gasPrice", required = true) Long gasPrice, // 即 rapid OR fast OR standard OR slow 对应的值
            @RequestParam(value = "amount", required = true) BigDecimal amount // 转帐数量, 单位是 erc20代币单位
    ) {
        return ethEstimateGasRMi.estimateGasLimitsForETH2TokenSwaper(ethnode, fromAddr, contractAddr, uniswapExchangeAddr, gasPrice, amount);
    }
    
    // #### Wallet
    // 创建钱包, 返回钱包地址
    @RequestMapping(value = "/setup", method = RequestMethod.POST, consumes=MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<String[]> setup(
            @RequestParam(value = "seedCode", required = true) String seedCode,
            @RequestParam(value = "passwd", required = true) String passwd) {
        return walletRMi.setup(seedCode, passwd);
    }

    // 创建钱包, 返回钱包地址
    @RequestMapping(value = "/seedCode", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<String> seedCode(
            @RequestParam(value = "wordcount", required = false, defaultValue = "12") Integer wordcount) {
        return walletRMi.seedCode(wordcount);
    }
    
    // ### uniswap v2
    /**
     * 查询默认支持的 token 列表
     * @param chainId
     * @return
     */
    @RequestMapping(value = "/default-token-list", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<List<Token>> defaultTokenList(
            @RequestParam(value = "chainId", required=false, defaultValue="mainnet") String chainId,
            @RequestParam(value = "symbol", required=false) String symbol)  {
        return univ2RMi.defaultTokenList(chainId, symbol);
    }

}
