package com.microee.ethdix.app.service;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import com.microee.ethdix.rmi.UniSwapV2TradeClient;
import com.microee.plugin.response.R;

@Service
public class ETHUniSwapV2TradeService {

    @Autowired
    private UniSwapV2TradeClient univ2TradeClient;

    public R<Map<String, Object>> eth2TokenSwap(
            @RequestParam("tokenAddr") String tokenAddr,
            @RequestBody String eth2TokenSwapVo) {
        return univ2TradeClient.eth2TokenSwap(tokenAddr, eth2TokenSwapVo);
    }
    
}
