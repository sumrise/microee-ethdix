package com.microee.ethdix.interfaces.univ2;

import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import com.microee.plugin.response.R;

public interface IETHUniSwapV2SDKRMi {

    // 根据token地址查询 token 对象
    @RequestMapping(value = "/token", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<Map<String, Object>> token(
            @RequestParam("tokenAddr") String tokenAddr) ;

    // 根据代币查询其对应的 usdc 价格 [usdc 是计价货币] 
    @RequestMapping(value = "/usdcPrice", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<String> usdcPrice(
            @RequestParam("address") String address, @RequestParam(value = "decimals", required=false) Integer decimals) ;

    @RequestMapping(value = "/pair", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<Map<String, Object>> pair(
            @RequestParam(value = "ethnode", required = false) String ethnode,
            @RequestParam(value = "chainId", required = false, defaultValue = "mainnet") String chainId,
            @RequestParam("tokenA") String tokenA,
            @RequestParam("tokenB") String tokenB, 
            @RequestParam(value = "method", required=false) String method) ;
    
    // 查询交易对合约地址
    @RequestMapping(value = "/pair/getPairAddress", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<String> getPairAddress(
            @RequestParam(value = "ethnode", required = false) String ethnode,
            @RequestParam(value = "chainId", required = false, defaultValue = "mainnet") String chainId,
            @RequestParam("tokenA") String tokenA,
            @RequestParam("tokenB") String tokenB) ;

    // 查询价格
    @RequestMapping(value = "/pair/priceOf", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<String> priceOf(
            @RequestParam(value = "ethnode", required = false) String ethnode,
            @RequestParam(value = "chainId", required = false, defaultValue = "mainnet") String chainId,
            @RequestParam("tokenA") String tokenA,
            @RequestParam("tokenB") String tokenB,
            @RequestParam("of") String of) ;

    // 根据输入数量计算输出数量
    @RequestMapping(value = "/pair/getOutputAmount", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<String> getOutputAmount(
            @RequestParam(value = "ethnode", required = false) String ethnode,
            @RequestParam(value = "chainId", required = false, defaultValue = "mainnet") String chainId,
            @RequestParam("tokenA") String tokenA,
            @RequestParam("tokenB") String tokenB,
            @RequestParam(value = "tokenAInputAmount", required=false) String tokenAInputAmount,
            @RequestParam(value = "tokenBInputAmount", required=false) String tokenBInputAmount) ;

    // 根据输出数量计算输入数量
    @RequestMapping(value = "/pair/getInputAmount", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<String> getInputAmount(
            @RequestParam(value = "ethnode", required = false) String ethnode,
            @RequestParam(value = "chainId", required = false, defaultValue = "mainnet") String chainId,
            @RequestParam("tokenA") String tokenA,
            @RequestParam("tokenB") String tokenB,
            @RequestParam(value = "tokenAOutputAmount", required=false) String tokenAOutputAmount,
            @RequestParam(value = "tokenBOutputAmount", required=false) String tokenBOutputAmount) ;

    // ..
    @RequestMapping(value = "/route", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<Map<String, Object>> route(
            @RequestParam("tokenA") String tokenA,
            @RequestParam("tokenB") String tokenB) ;
    // ..
    @RequestMapping(value = "/trade", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<Map<String, Object>> trade(
            @RequestParam("tokenA") String tokenA,
            @RequestParam("tokenB") String tokenB) ;
}
