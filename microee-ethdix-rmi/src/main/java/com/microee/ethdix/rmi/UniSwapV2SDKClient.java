package com.microee.ethdix.rmi;

import org.springframework.cloud.netflix.feign.FeignClient;
import com.microee.ethdix.interfaces.IUniSwapV2SDKRMi;

@FeignClient(name = "microee-ethdix-uniswapv2-sdk",
        url = "${micro.services.microee-ethdix-uniswapv2-sdk.listOfServers}",
        path = "/univ2",
        configuration = UniSwapV2ClientConfiguration.class)
public interface UniSwapV2SDKClient extends IUniSwapV2SDKRMi {

}
