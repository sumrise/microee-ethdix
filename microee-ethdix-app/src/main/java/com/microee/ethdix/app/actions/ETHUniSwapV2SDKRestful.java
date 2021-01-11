package com.microee.ethdix.app.actions;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.microee.ethdix.app.service.ETHUniSwapV2SDKService;
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
            @RequestParam("tokenA") String tokenA,
            @RequestParam("tokenB") String tokenB) {
        return R.ok(univ2SDKService.pair(tokenA, tokenB));
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
