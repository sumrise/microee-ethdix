package com.microee.ethdix.rmi;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.cloud.netflix.ribbon.RibbonClient;
import com.microee.ethdix.interfaces.IETHBalanceRMi;

@RibbonClient(name = "microee-ethdix-app")
@FeignClient(name = "microee-ethdix-app", path = "/balance", configuration = ETHDixRMiConfiguration.class)
public interface ETHBalanceRMi extends IETHBalanceRMi {

}
