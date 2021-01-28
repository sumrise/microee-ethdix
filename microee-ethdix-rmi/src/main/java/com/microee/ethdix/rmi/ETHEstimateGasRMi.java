package com.microee.ethdix.rmi;

import org.springframework.cloud.netflix.feign.FeignClient;
import com.microee.ethdix.interfaces.IETHEstimateGasRMi;

@FeignClient(name = "microee-ethdix-app", path = "/estimates", configuration = ETHDixRMiConfiguration.class)
public interface ETHEstimateGasRMi extends IETHEstimateGasRMi {

}
