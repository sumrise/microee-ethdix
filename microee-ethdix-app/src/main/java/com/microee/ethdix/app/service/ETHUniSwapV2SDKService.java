package com.microee.ethdix.app.service;

import com.microee.ethdix.oem.eth.entity.Token;
import com.microee.ethdix.rmi.UniSwapV2SDKClient;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ETHUniSwapV2SDKService {
    
    @Autowired
    private UniSwapV2SDKClient univ2SDKClient;

    public List<Token> defaultTokenList(short chainId, String symbol) {
        return univ2SDKClient.defaultTokenList(chainId, symbol).getData();
    }
    
    public Map<String, Object> token(String tokenAddr) {
        return univ2SDKClient.token(tokenAddr).getData();
    }
    
    public Map<String, Object> pair(String tokenA, String tokenB) {
        return univ2SDKClient.pair(tokenA, tokenB).getData();
    }
    
    public Map<String, Object> route(String tokenA, String tokenB) {
        return univ2SDKClient.route(tokenA, tokenB).getData();
    }
    
    public Map<String, Object> trade(String tokenA, String tokenB) {
        return univ2SDKClient.trade(tokenA, tokenB).getData();
    }
}
