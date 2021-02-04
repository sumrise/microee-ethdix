package com.uniswap.dixx.rmi;

import java.util.List;
import java.util.Map;
import org.json.JSONObject;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import com.microee.ethdix.oem.eth.entity.Token;
import com.microee.plugin.response.R;

@FeignClient(name = "microee-ethdix-uniswapv2-sdk",
        url = "${micro.services.uniswap-dixx.listOfServers}",
        path = "/univ2-sdk",
        configuration = UniSwapV2ClientConfiguration.class)
public interface UniSwapV2SDKClient {

    // 查询默认支持的代币列表
    @RequestMapping(value = "/default-token-list", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<List<Token>> defaultTokenList(@RequestParam("chainId") short chainId, @RequestParam("symbol") String symbol);
    
    // 根据代币地址查询代币信息
    @RequestMapping(value = "/token", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<Map<String, Object>> token(@RequestParam("tokenAddr") String tokenAddr);

    // 查询传入的代币对应的 USDC 价格
    @RequestMapping(value = "/usdcPrice", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<String> usdcPrice(@RequestBody JSONObject param);

    // 根据两个代币地址查询交易对对象
    @RequestMapping(value = "/pair", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<Map<String, Object>> pair(@RequestParam("tokenA") String tokenA, @RequestParam("tokenB") String tokenB, @RequestParam(value = "method", required=false) String method);

    // 根据2个代币查询交易对地址
    @RequestMapping(value = "/pair/getPairAddress", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<String> getPairAddress(@RequestParam("tokenA") String tokenA, @RequestParam("tokenB") String tokenB);

    // 查询当前交易对兑换价格
    @RequestMapping(value = "/pair/priceOf", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<String> priceOf(@RequestParam("tokenA") String tokenA, @RequestParam("tokenB") String tokenB, @RequestParam("of") String of);

    // 根据输入数量计算输出数量
    @RequestMapping(value = "/pair/getOutputAmount", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<String> getOutputAmount(
            @RequestParam("tokenA") String tokenA, @RequestParam("tokenB") String tokenB, 
            @RequestParam(value="tokenAInputAmount", required=false) String tokenAInputAmount, @RequestParam(value="tokenBInputAmount", required=false) String tokenBInputAmount);

    // 根据输出数量计算输入数量
    @RequestMapping(value = "/pair/getInputAmount", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<String> getInputAmount(
            @RequestParam("tokenA") String tokenA, @RequestParam("tokenB") String tokenB, 
            @RequestParam(value="tokenAOutputAmount", required=false) String tokenAOutputAmount, @RequestParam(value="tokenBOutputAmount", required=false) String tokenBOutputAmount);

    // 根据两个代币地址查询 route 对象
    @RequestMapping(value = "/route", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<Map<String, Object>> route(@RequestParam("tokenA") String tokenA, @RequestParam("tokenB") String tokenB);

    @Deprecated
    @RequestMapping(value = "/trade", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<Map<String, Object>> trade(@RequestParam("tokenA") String tokenA, @RequestParam("tokenB") String tokenB);

}
