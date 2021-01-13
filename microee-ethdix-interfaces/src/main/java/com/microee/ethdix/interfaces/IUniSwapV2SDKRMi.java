package com.microee.ethdix.interfaces;

import java.util.List;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import com.microee.ethdix.oem.eth.entity.Token;
import com.microee.plugin.response.R;

// UniSwapV2 SDK RMi
public interface IUniSwapV2SDKRMi {
    
    @RequestMapping(value = "/default-token-list", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<List<Token>> defaultTokenList(@RequestParam("chainId") short chainId, @RequestParam("symbol") String symbol);
    
    @RequestMapping(value = "/token", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<Map<String, Object>> token(@RequestParam("tokenAddr") String tokenAddr);

    @RequestMapping(value = "/pair", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<Map<String, Object>> pair(@RequestParam("tokenA") String tokenA, @RequestParam("tokenB") String tokenB);

    @RequestMapping(value = "/pair/getPairAddress", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<String> getPairAddress(@RequestParam("tokenA") String tokenA, @RequestParam("tokenB") String tokenB);

    @RequestMapping(value = "/route", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<Map<String, Object>> route(@RequestParam("tokenA") String tokenA, @RequestParam("tokenB") String tokenB);

    @RequestMapping(value = "/trade", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<Map<String, Object>> trade(@RequestParam("tokenA") String tokenA, @RequestParam("tokenB") String tokenB);

}
