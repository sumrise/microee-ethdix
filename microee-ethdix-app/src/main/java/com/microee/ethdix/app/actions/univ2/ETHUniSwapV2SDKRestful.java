package com.microee.ethdix.app.actions.univ2;

import java.util.Map;
import org.assertj.core.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.microee.ethdix.app.service.ETHUniSwapV2SDKService;
import com.microee.ethdix.oem.eth.enums.ChainId;
import com.microee.plugin.commons.RegexUtils;
import com.microee.plugin.response.R;

@RestController
@RequestMapping("/univ2-sdk")
public class ETHUniSwapV2SDKRestful {

    @Autowired
    private ETHUniSwapV2SDKService univ2SDKService;
    
    // 根据token地址查询 token 对象
    @RequestMapping(value = "/token", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<Map<String, Object>> token(
            @RequestParam("tokenAddr") String tokenAddr) {
        return R.ok(univ2SDKService.token(tokenAddr));
    }

    // 根据代币查询其对应的 usdc 价格 [usdc 是计价货币] 
    @RequestMapping(value = "/usdcPrice", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<String> usdcPrice(
            @RequestParam("address") String address, @RequestParam(value = "decimals", required=false) Integer decimals) {
        Assertions.assertThat(RegexUtils.isAddress(address)).withFailMessage("%s 有误", "address").isTrue();
        return univ2SDKService.usdcPrice(address, decimals);
    }

    @RequestMapping(value = "/pair", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<Map<String, Object>> pair(
            @RequestParam(value = "ethnode", required = false) String ethnode,
            @RequestParam(value = "chainId", required = false, defaultValue = "mainnet") String chainId,
            @RequestParam("tokenA") String tokenA,
            @RequestParam("tokenB") String tokenB, 
            @RequestParam(value = "method", required=false) String method) {
        Assertions.assertThat(ChainId.get(chainId)).withFailMessage("%s 有误", "chainId").isNotNull();
        Assertions.assertThat(RegexUtils.isAddress(tokenA)).withFailMessage("%s 有误", "tokenA").isTrue();
        Assertions.assertThat(RegexUtils.isAddress(tokenB)).withFailMessage("%s 有误", "tokenB").isTrue();
        return univ2SDKService.pair(ChainId.get(chainId), ethnode, tokenA, tokenB, method);
    }
    
    // 查询交易对合约地址
    @RequestMapping(value = "/pair/getPairAddress", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<String> getPairAddress(
            @RequestParam(value = "ethnode", required = false) String ethnode,
            @RequestParam(value = "chainId", required = false, defaultValue = "mainnet") String chainId,
            @RequestParam("tokenA") String tokenA,
            @RequestParam("tokenB") String tokenB) {
        Assertions.assertThat(ChainId.get(chainId)).withFailMessage("%s 有误", "chainId").isNotNull();
        Assertions.assertThat(RegexUtils.isAddress(tokenA)).withFailMessage("%s 有误", "tokenA").isTrue();
        Assertions.assertThat(RegexUtils.isAddress(tokenB)).withFailMessage("%s 有误", "tokenB").isTrue();
        return univ2SDKService.getPairAddress(ChainId.get(chainId), ethnode, tokenA, tokenB);
    }

    // 查询价格
    @RequestMapping(value = "/pair/priceOf", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<String> priceOf(
            @RequestParam(value = "ethnode", required = false) String ethnode,
            @RequestParam(value = "chainId", required = false, defaultValue = "mainnet") String chainId,
            @RequestParam("tokenA") String tokenA,
            @RequestParam("tokenB") String tokenB,
            @RequestParam("of") String of) {
        Assertions.assertThat(ChainId.get(chainId)).withFailMessage("%s 有误", "chainId").isNotNull();
        Assertions.assertThat(RegexUtils.isAddress(tokenA)).withFailMessage("%s 有误", "tokenA").isTrue();
        Assertions.assertThat(RegexUtils.isAddress(tokenB)).withFailMessage("%s 有误", "tokenB").isTrue();
        return univ2SDKService.priceOf(ChainId.get(chainId), ethnode, tokenA, tokenB, of);
    }

    // 根据输入数量计算输出数量
    @RequestMapping(value = "/pair/getOutputAmount", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<String> getOutputAmount(
            @RequestParam(value = "ethnode", required = false) String ethnode,
            @RequestParam(value = "chainId", required = false, defaultValue = "mainnet") String chainId,
            @RequestParam("tokenA") String tokenA,
            @RequestParam("tokenB") String tokenB,
            @RequestParam(value = "tokenAInputAmount", required=false) String tokenAInputAmount,
            @RequestParam(value = "tokenBInputAmount", required=false) String tokenBInputAmount) {
        Assertions.assertThat(ChainId.get(chainId)).withFailMessage("%s 有误", "chainId").isNotNull();
        Assertions.assertThat(RegexUtils.isAddress(tokenA)).withFailMessage("%s 有误", "tokenA").isTrue();
        Assertions.assertThat(RegexUtils.isAddress(tokenB)).withFailMessage("%s 有误", "tokenB").isTrue();
        Assertions.assertThat((tokenAInputAmount == null || tokenAInputAmount.isEmpty()) && (tokenBInputAmount == null || tokenBInputAmount.isEmpty())).withFailMessage("`%s` and `%s` 二传一", "tokenAInputAmount", "tokenBInputAmount").isFalse();
        return R.ok(univ2SDKService.getOutputAmount(ChainId.get(chainId), ethnode, tokenA, tokenB, tokenAInputAmount, tokenBInputAmount));
    }

    // 根据输出数量计算输入数量
    @RequestMapping(value = "/pair/getInputAmount", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<String> getInputAmount(
            @RequestParam(value = "ethnode", required = false) String ethnode,
            @RequestParam(value = "chainId", required = false, defaultValue = "mainnet") String chainId,
            @RequestParam("tokenA") String tokenA,
            @RequestParam("tokenB") String tokenB,
            @RequestParam(value = "tokenAOutputAmount", required=false) String tokenAOutputAmount,
            @RequestParam(value = "tokenBOutputAmount", required=false) String tokenBOutputAmount) {
        Assertions.assertThat(ChainId.get(chainId)).withFailMessage("%s 有误", "chainId").isNotNull();
        Assertions.assertThat(RegexUtils.isAddress(tokenA)).withFailMessage("%s 有误", "tokenA").isTrue();
        Assertions.assertThat(RegexUtils.isAddress(tokenB)).withFailMessage("%s 有误", "tokenB").isTrue();
        Assertions.assertThat((tokenAOutputAmount == null || tokenAOutputAmount.isEmpty()) && (tokenBOutputAmount == null || tokenBOutputAmount.isEmpty())).withFailMessage("`%s` and `%s` 二传一", "tokenAOutputAmount", "tokenBOutputAmount").isFalse();
        return R.ok(univ2SDKService.getInputAmount(ChainId.get(chainId), ethnode, tokenA, tokenB, tokenAOutputAmount, tokenBOutputAmount)); 
    }

    // ..
    @RequestMapping(value = "/route", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<Map<String, Object>> route(
            @RequestParam("tokenA") String tokenA,
            @RequestParam("tokenB") String tokenB) {
        return R.ok(univ2SDKService.route(tokenA, tokenB));
    }

    // ..
    @RequestMapping(value = "/trade", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<Map<String, Object>> trade(
            @RequestParam("tokenA") String tokenA,
            @RequestParam("tokenB") String tokenB) {
        return R.ok(univ2SDKService.trade(tokenA, tokenB));
    }

}
