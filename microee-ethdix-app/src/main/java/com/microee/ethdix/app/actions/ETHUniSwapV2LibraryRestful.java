package com.microee.ethdix.app.actions;

import com.microee.ethdix.j3.uniswap.UniswapV2Library;
import com.microee.plugin.response.R;
import org.assertj.core.api.Assertions;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.web3j.abi.datatypes.Address;

// UniSwapV2 库函数
// https://uniswap.org/docs/v2/smart-contracts/library/#internal-functions
// https://github.com/Uniswap/uniswap-v2-periphery/blob/master/contracts/libraries/UniswapV2Library.sol
@RestController
@RequestMapping("/uniswapv2/library")
public class ETHUniSwapV2LibraryRestful {
    
    // 返回两个排好顺序的代币地址
    // Calculates the address for a pair without making any external calls
    @RequestMapping(value = "/sortTokens", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<String[]> sortTokens(
            @RequestParam(value = "tokenA", required = true) String tokenA,
            @RequestParam(value = "tokenB", required = true) String tokenB
    ) {
        Assertions.assertThat(tokenA).withFailMessage("`tokenA` 必传").isNotBlank();
        Assertions.assertThat(tokenB).withFailMessage("`tokenB` 必传").isNotBlank();
        Address[] sortedAddrs = UniswapV2Library.sortTokens(tokenA, tokenB);
        return R.ok(new String[] { sortedAddrs[0].getValue(), sortedAddrs[1].getValue() } );
    }
    
}
