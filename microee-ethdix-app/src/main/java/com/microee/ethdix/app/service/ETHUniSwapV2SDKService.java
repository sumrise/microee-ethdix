package com.microee.ethdix.app.service;

import com.microee.ethdix.rmi.UniSwapV2SDKClient;
import com.microee.plugin.response.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ETHUniSwapV2SDKService {
    
    @Autowired
    private UniSwapV2SDKClient univ2SDKClient;
    
    public String token() {
        R<String> tokenResult = univ2SDKClient.token();
        return tokenResult.getData();
    }
    
    public String pair() {
        R<String> pairResult = univ2SDKClient.pair();
        return pairResult.getData();
    }
    
    public String route() {
        R<String> routeResult = univ2SDKClient.route();
        return routeResult.getData();
    }
    
    public String trade() {
        R<String> tradeResult = univ2SDKClient.trade();
        return tradeResult.getData();
    }
    
    public String fractions() {
        R<String> fractionsResult = univ2SDKClient.fractions();
        return fractionsResult.getData();
    }
    
    public String fetcher() {
        R<String> fetcherResult = univ2SDKClient.fetcher();
        return fetcherResult.getData();
    }
    
}
