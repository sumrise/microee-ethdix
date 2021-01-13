package com.microee.ethdix.app.actions;

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

    @RequestMapping(value = "/token", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<Map<String, Object>> token(
            @RequestParam("tokenAddr") String tokenAddr) {
        return R.ok(univ2SDKService.token(tokenAddr));
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

    @RequestMapping(value = "/getPairAddress", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
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

    @RequestMapping(value = "/route", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<Map<String, Object>> route(
            @RequestParam("tokenA") String tokenA,
            @RequestParam("tokenB") String tokenB) {
        return R.ok(univ2SDKService.route(tokenA, tokenB));
    }

    @RequestMapping(value = "/trade", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<Map<String, Object>> trade(
            @RequestParam("tokenA") String tokenA,
            @RequestParam("tokenB") String tokenB) {
        return R.ok(univ2SDKService.trade(tokenA, tokenB));
    }

}
