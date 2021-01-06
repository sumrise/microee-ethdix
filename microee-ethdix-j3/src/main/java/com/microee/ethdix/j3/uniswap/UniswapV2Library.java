package com.microee.ethdix.j3.uniswap;

import com.microee.plugin.response.R;
import com.microee.plugin.response.exception.RestException;
import org.web3j.abi.datatypes.Address;

// UniSwapV2 库函数
public class UniswapV2Library {
    
    // 返回两个排好顺序的代币地址
    public static Address[] sortTokens(String tokenA, String tokenB) {
        if (tokenA.equalsIgnoreCase(tokenB)) {
            throw new RestException(R.ILLEGAL, "两个地址不能相等");
        }
        Address[] sortedAddrs = new Address[2];
        Address tokenAAddr = new Address(tokenA);
        Address tokenBAddr = new Address(tokenB);
        if (tokenAAddr.toUint().getValue().longValue() < tokenBAddr.toUint().getValue().longValue()) {
            sortedAddrs[0] = tokenAAddr;
            sortedAddrs[1] = tokenBAddr;
        } else {
            sortedAddrs[0] = tokenBAddr;
            sortedAddrs[1] = tokenAAddr;
        }
        return sortedAddrs;
    }
    
}
