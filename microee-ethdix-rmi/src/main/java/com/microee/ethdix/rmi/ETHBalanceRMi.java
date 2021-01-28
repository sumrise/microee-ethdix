package com.microee.ethdix.rmi;

import org.springframework.cloud.netflix.feign.FeignClient;
import com.microee.ethdix.interfaces.IETHBalanceRMi;

@FeignClient(name = "microee-ethdix-app", path = "/balance", configuration = ETHDixRMiConfiguration.class)
public interface ETHBalanceRMi extends IETHBalanceRMi {

}
