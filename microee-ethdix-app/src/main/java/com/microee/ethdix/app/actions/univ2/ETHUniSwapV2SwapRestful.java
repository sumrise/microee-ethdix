package com.microee.ethdix.app.actions.univ2;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.web3j.abi.datatypes.Address;
import com.microee.ethdix.app.components.Web3JFactory;
import com.microee.ethdix.app.service.ETHUniSwapV2SDKService;
import com.microee.ethdix.j3.contract.ERC20ContractQuery;
import com.microee.ethdix.j3.contract.ETHInputEncoder;
import com.microee.ethdix.j3.contract.RemoteCallFunction;
import com.microee.ethdix.j3.uniswap.UniswapV2Route02Contract;
import com.microee.ethdix.oem.eth.enums.ChainId;
import com.microee.plugin.commons.RegexUtils;
import com.microee.plugin.response.R;

@RestController
@RequestMapping("/univ2-swap")
public class ETHUniSwapV2SwapRestful {

    @Autowired
    private Web3JFactory web3JFactory;

    @Autowired
    private ETHUniSwapV2SDKService univ2SDKService;
    
    // 根据输入数据计算 UniSwapV2 兑换参数
    @RequestMapping(value = "/getEth2TokenParams", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<Map<String, Object>> getEth2TokenParams(
            @RequestParam(value = "chainId", required = false, defaultValue = "mainnet") String chainId,
            @RequestParam("tokenAddr") String tokenAddr,
            @RequestParam("ethAmount") String ethAmount,
            @RequestParam("slippageTolerance") Integer slippageTolerance) {
        // We use this slippage tolerance to calculate the minumum amount of DAI we must receive before our trade reverts, thanks to minimumAmountOut. 
        // The value is the amount of ETH that must be included as the msg.value in our transaction.
        // const amountOutMin = trade.minimumAmountOut(slippageTolerance).raw
        // Double slippageTolerance = 0.2d;
        Assertions.assertThat(ChainId.get(chainId)).withFailMessage("%s 有误", "chainId").isNotNull();
        Assertions.assertThat(RegexUtils.isFloat(ethAmount)).withFailMessage("%s 有误", "ethAmount").isTrue();
        Assertions.assertThat(slippageTolerance).withFailMessage("%s 有误", "slippageTolerance >0 and <100").isBetween(0, 100);
        return R.ok(univ2SDKService.getEth2TokenParams(ChainId.get(chainId), tokenAddr, ethAmount, slippageTolerance)); 
    }
    
    // ETH与代币兑换构建签名
    @RequestMapping(value = "/eth2TokenSwapSign", method = RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<String> eth2TokenSwap(
            @RequestParam(value = "chainId", required = false, defaultValue = "mainnet") String chainId,
            @RequestParam(value = "ethnode", required = true) String ethnode, // 以太坊节点地址
            @RequestParam(value = "router02Addr", required = false, defaultValue = "0x7a250d5630B4cF539739dF2C5dAcb4c659F2488D") String router02Addr,
            @RequestParam(value = "tokenAddr", required = true) String tokenAddr, // erc20 合约地址
            @RequestParam(value = "gasPrice", required = true) Long gasPrice, // 即 rapid OR fast OR standard OR slow 对应的值
            @RequestParam(value = "gasLimit", required = false, defaultValue = "21000") Long gasLimit,
            @RequestParam(value = "ethAmount", required = true) String ethAmount, // 转帐数量, 单位是 erc20代币单位
            @RequestParam(value = "amountOutMin", required = true) String amountOutMin, // 转帐数量, 单位是 erc20代币单位
            @RequestParam(value = "fromAddress", required = true) String fromAddress, // 账户地址
            @RequestParam(value = "deadline", required = false, defaultValue="20") Integer deadline // 默认20分钟
    ) {
        Assertions.assertThat(ChainId.get(chainId)).withFailMessage("%s 有误", "chainId").isNotNull();
        Assertions.assertThat(ethnode).withFailMessage("%s 必传", "ethnode").isNotBlank();
        Assertions.assertThat(RegexUtils.isAddress(tokenAddr)).withFailMessage("%s 有误", "tokenAddr").isTrue();
        Assertions.assertThat(RegexUtils.isFloat(ethAmount)).withFailMessage("%s 有误", "ethAmount").isTrue();
        Assertions.assertThat(RegexUtils.isFloat(amountOutMin)).withFailMessage("%s 有误", "amountOutMin").isTrue();
        Assertions.assertThat(RegexUtils.isAddress(fromAddress)).withFailMessage("%s 有误", "fromAddress").isTrue();
        Assertions.assertThat(deadline).withFailMessage("%s 有误", "slippageTolerance >1 and <60").isBetween(1, 60);
        BigInteger tokenDecimals = RemoteCallFunction.build(new ERC20ContractQuery(tokenAddr, web3JFactory.getByEthNode(ethnode)).decimals()).call();
        Address wethAddr = RemoteCallFunction.build(new UniswapV2Route02Contract(router02Addr, web3JFactory.getByEthNode(ethnode)).WETH()).call();
        BigInteger amountOutMinLong = new BigDecimal(amountOutMin).pow(tokenDecimals.intValue()).toBigInteger();
        //BigInteger ethAmountLong = new BigDecimal(ethAmount).pow(18).toBigInteger();
        String inputData = ETHInputEncoder.getInputDataForSwapExactETHForTokens(amountOutMinLong, wethAddr.getValue(), tokenAddr, fromAddress, deadline);
        return R.ok(inputData).message(amountOutMinLong + "");
    }
    
}
