package com.microee.ethdix.rmi;

import org.springframework.cloud.netflix.feign.FeignClient;
import com.microee.ethdix.interfaces.IUniSwapV2SwapParamRMi;

@FeignClient(name = "microee-ethdix-uniswapv2-sdk",
        url = "${micro.services.microee-ethdix-uniswapv2-sdk.listOfServers}",
        path = "/univ2-swap-params",
        configuration = UniSwapV2ClientConfiguration.class)
public interface UniSwapV2SwapParamClient extends IUniSwapV2SwapParamRMi {

}
