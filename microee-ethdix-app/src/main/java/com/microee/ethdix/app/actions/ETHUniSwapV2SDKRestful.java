package com.microee.ethdix.app.actions;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.microee.ethdix.app.service.ETHUniSwapV2SDKService;
import com.microee.ethdix.interfaces.IUniSwapV2SDKRMi;
import com.microee.plugin.response.R;

@RestController
@RequestMapping("/univ2")
public class ETHUniSwapV2SDKRestful implements IUniSwapV2SDKRMi{

    @Autowired
    private ETHUniSwapV2SDKService univ2SDKService;

    @Override
    @RequestMapping(value = "/token", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<Map<String, Object>> token(
            @RequestParam("tokenAddr") String tokenAddr) {
        return R.ok(univ2SDKService.token(tokenAddr));
    }

    @Override
    @RequestMapping(value = "/pair", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<Map<String, Object>> pair(
            @RequestParam("tokenA") String tokenA,
            @RequestParam("tokenB") String tokenB) {
        return R.ok(univ2SDKService.pair(tokenA, tokenB));
    }

    @Override
    @RequestMapping(value = "/route", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<Map<String, Object>> route(
            @RequestParam("tokenA") String tokenA,
            @RequestParam("tokenB") String tokenB) {
        return R.ok(univ2SDKService.route(tokenA, tokenB));
    }

    @Override
    @RequestMapping(value = "/trade", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<Map<String, Object>> trade(
            @RequestParam("tokenA") String tokenA,
            @RequestParam("tokenB") String tokenB) {
        return R.ok(univ2SDKService.trade(tokenA, tokenB));
    }

}
