package com.uniswap.dixx.rmi;

import java.util.Map;
import org.json.JSONObject;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import com.microee.plugin.response.R;

@FeignClient(name = "microee-ethdix-uniswapv2-sdk",
        url = "${micro.services.uniswap-dixx.listOfServers}",
        path = "/univ2-swap-params",
        configuration = UniSwapV2ClientConfiguration.class)
public interface UniSwapV2SwapParamClient {

    @RequestMapping(value = "/eth2token", method = RequestMethod.POST, consumes=MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<Map<String, Object>> eth2token(
            @RequestParam("tokenAddr") String tokenAddr,
            @RequestBody JSONObject params);
}
