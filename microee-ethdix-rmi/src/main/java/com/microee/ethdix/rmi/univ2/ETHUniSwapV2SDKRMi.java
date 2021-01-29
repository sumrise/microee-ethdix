package com.microee.ethdix.rmi.univ2;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.cloud.netflix.ribbon.RibbonClient;
import com.microee.ethdix.interfaces.univ2.IETHUniSwapV2SDKRMi;
import com.microee.ethdix.rmi.ETHDixRMiConfiguration;

@RibbonClient(name = "microee-ethdix-app")
@FeignClient(name = "microee-ethdix-app", path = "/univ2-sdk", configuration = ETHDixRMiConfiguration.class)
public interface ETHUniSwapV2SDKRMi extends IETHUniSwapV2SDKRMi {

}
