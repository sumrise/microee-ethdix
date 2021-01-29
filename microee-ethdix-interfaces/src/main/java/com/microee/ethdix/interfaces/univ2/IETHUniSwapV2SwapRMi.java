package com.microee.ethdix.interfaces.univ2;

import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import com.microee.plugin.response.R;

public interface IETHUniSwapV2SwapRMi {

    // 根据输入数据计算 UniSwapV2 兑换参数
    @RequestMapping(value = "/getEth2TokenParams", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<Map<String, Object>> getEth2TokenParams(
            @RequestParam(value = "chainId", required = false, defaultValue = "mainnet") String chainId,
            @RequestParam("tokenAddr") String tokenAddr,
            @RequestParam("ethAmount") String ethAmount,
            @RequestParam("slippageTolerance") Integer slippageTolerance) ;
    
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
    ) ;
}
