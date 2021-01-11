package com.microee.ethdix.app.service;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.microee.ethdix.rmi.UniSwapV2TradeClient;
import com.microee.plugin.response.R;

@Service
public class ETHUniSwapV2TradeService {

    @Autowired
    private UniSwapV2TradeClient univ2TradeClient;

    public R<Map<String, Object>> eth2TokenSwapGetParams(String tokenAddr, String eth2TokenSwapVo) {
        return univ2TradeClient.eth2TokenSwapGetParams(tokenAddr, eth2TokenSwapVo);
    }

    public R<Map<String, Object>> eth2TokenSwapSendTranaction(String ethnode, String router02Addr, String eth2TokenSwapGetParams) {
        return univ2TradeClient.eth2TokenSwapSendTranaction(ethnode, router02Addr, eth2TokenSwapGetParams);
    }

}
