package com.uniswap.dixx.rmi;

import org.springframework.cloud.netflix.feign.FeignClient;
import com.microee.ethdix.interfaces.IUniSwapV2SDKRMi;

@FeignClient(name = "microee-ethdix-uniswapv2-sdk",
        url = "${micro.services.uniswap-dixx.listOfServers}",
        path = "/univ2-sdk",
        configuration = UniSwapV2ClientConfiguration.class)
public interface UniSwapV2SDKClient extends IUniSwapV2SDKRMi {

}
