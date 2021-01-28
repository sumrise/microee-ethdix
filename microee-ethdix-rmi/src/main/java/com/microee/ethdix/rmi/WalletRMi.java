package com.microee.ethdix.rmi;

import org.springframework.cloud.netflix.feign.FeignClient;
import com.microee.ethdix.interfaces.IWalletRMi;

@FeignClient(name = "microee-ethdix-app", path = "/wallet", configuration = ETHDixRMiConfiguration.class)
public interface WalletRMi extends IWalletRMi {

}
