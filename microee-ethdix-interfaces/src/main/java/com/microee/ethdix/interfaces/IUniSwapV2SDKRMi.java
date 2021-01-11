package com.microee.ethdix.interfaces;

import com.microee.plugin.response.R;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

// UniSwapV2 SDK RMi
public interface IUniSwapV2SDKRMi {

    @RequestMapping(value = "/token-sdk", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<Map<String, Object>> token(@RequestParam("tokenAddr") String tokenAddr);

    @RequestMapping(value = "/pair-sdk", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<Map<String, Object>> pair(
            @RequestParam("tokenA") String tokenA,
            @RequestParam("tokenB") String tokenB);

    @RequestMapping(value = "/route-sdk", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<Map<String, Object>> route(
            @RequestParam("tokenA") String tokenA,
            @RequestParam("tokenB") String tokenB);

    @RequestMapping(value = "/trade-sdk", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<Map<String, Object>> trade(
            @RequestParam("tokenA") String tokenA,
            @RequestParam("tokenB") String tokenB);

}
