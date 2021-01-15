package com.microee.ethdix.app.service;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.microee.ethdix.app.components.Web3JFactory;
import com.microee.ethdix.j3.contract.ERC20ContractQuery;
import com.microee.ethdix.j3.contract.RemoteCallFunction;
import com.microee.ethdix.oem.eth.entity.Token;
import com.microee.ethdix.oem.eth.enums.ChainId;
import com.microee.ethdix.rmi.UniSwapV2SDKClient;
import com.microee.plugin.commons.RegexUtils;
import com.microee.plugin.response.R;

@Service
public class ETHUniSwapV2SDKService {

    @Autowired
    private Web3JFactory web3JFactory;
    
    @Autowired
    private UniSwapV2SDKClient univ2SDKClient;

    public List<Token> defaultTokenList(short chainId, String symbol) {
        return univ2SDKClient.defaultTokenList(chainId, symbol).getData();
    }
    
    public Map<String, Object> token(String tokenAddr) {
        return univ2SDKClient.token(tokenAddr).getData();
    }
    
    public R<Map<String, Object>> pair(ChainId chainId, String ethnode, String tokenA, String tokenB, String method) {
        R<Map<String, Object>> result = univ2SDKClient.pair(tokenA, tokenB, method);
        return R.ok(result.getData()).message(this.getPairSymbol(chainId, ethnode, result.getMessage()));
    }
    
    public R<String> getPairAddress(ChainId chainId, String ethnode, String tokenA, String tokenB) {
        R<String> result = univ2SDKClient.getPairAddress(tokenA, tokenB);
        return R.ok(result.getData()).message(this.getPairSymbol(chainId, ethnode, result.getMessage()));
    }

    public R<String> priceOf(ChainId chainId, String ethnode, String tokenA, String tokenB, String of) {
        R<String> result = univ2SDKClient.priceOf(tokenA, tokenB, of); 
        return R.ok(result.getData()).message(this.getPairSymbol(chainId, ethnode, result.getMessage()));
    }

    public String getOutputAmount(ChainId chainId, String ethnode, String tokenA, String tokenB,
            String tokenAInputAmount, String tokenBInputAmount) {
        return univ2SDKClient.getOutputAmount(tokenA, tokenB, tokenAInputAmount, tokenBInputAmount).getData(); 
    }

    public String getInputAmount(ChainId chainId, String ethnode, String tokenA, String tokenB,
            String tokenAOutputAmount, String tokenBOutputAmount) {
        return univ2SDKClient.getInputAmount(tokenA, tokenB, tokenAOutputAmount, tokenBOutputAmount).getData(); 
    }
    
    public Map<String, Object> route(String tokenA, String tokenB) {
        return univ2SDKClient.route(tokenA, tokenB).getData();
    }
    
    public Map<String, Object> trade(String tokenA, String tokenB) {
        return univ2SDKClient.trade(tokenA, tokenB).getData();
    }
    
    public String getPairSymbol(ChainId chainId, String ethnode, String pairSymbol) {
        if (!pairSymbol.contains("/")) {
            return pairSymbol;
        }
        String symbolA = pairSymbol.split("/")[0];
        String symbolB = pairSymbol.split("/")[1];
        if (RegexUtils.isAddress(symbolA)) {
            symbolA = RemoteCallFunction.build(new ERC20ContractQuery(symbolA, web3JFactory.get(chainId, ethnode)).symbol()).call();
        }
        if (RegexUtils.isAddress(symbolB)) {
            symbolB = RemoteCallFunction.build(new ERC20ContractQuery(symbolB, web3JFactory.get(chainId, ethnode)).symbol()).call();
        }
        return String.format("%s/%s", symbolA, symbolB);
    }
    
}
