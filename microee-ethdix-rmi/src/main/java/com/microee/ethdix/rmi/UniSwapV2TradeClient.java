package com.microee.ethdix.rmi;

import org.springframework.cloud.netflix.feign.FeignClient;
import com.microee.ethdix.interfaces.IUniSwapV2TradeRMi;

@FeignClient(name = "microee-ethdix-uniswapv2-sdk",
        url = "${micro.services.microee-ethdix-uniswapv2-sdk.listOfServers}",
        path = "/univ2-trade",
        configuration = UniSwapV2ClientConfiguration.class)
public interface UniSwapV2TradeClient extends IUniSwapV2TradeRMi{

}
