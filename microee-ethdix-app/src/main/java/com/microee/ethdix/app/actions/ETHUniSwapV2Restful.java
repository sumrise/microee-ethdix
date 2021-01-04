package com.microee.ethdix.app.actions;

import org.assertj.core.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.web3j.protocol.Web3j;
import com.microee.ethdix.app.components.Web3JFactory;
import com.microee.ethdix.j3.Constrants;
import com.microee.ethdix.j3.contract.RemoteCallFunction;
import com.microee.ethdix.j3.factory.Web3jOfInstanceFactory;
import com.microee.ethdix.j3.uniswap.UniswapV2FactoryContract;
import com.microee.plugin.response.R;

// #### 在V2中
// 不再将ETH做为中间币, 只需调用路由合约上的 swapExactTokensForTokens 和 swapTokensForExactTokens 方法即可交易
// 上述方法名中的 Exact 表示你希望为交换中的哪一种代币设置限额。
// 如果你想用 DAI 买入一定数量的 ETH，就需要使用 swapTokensForExactTokens
// 另一方面如果你想用一定数量的 DAI 买入 ETH，就需要使用 swapExactTokensForTokens。
// UniSwapV2的智能和鱼就是采用了这种方法
// https://uniswap.org
@RestController
@RequestMapping("/uniswapv2")
public class ETHUniSwapV2Restful {

    @Autowired
    private Web3JFactory web3JFactory;

    // 根据网络类型随机返回一个节点地址
    @RequestMapping(value = "/getETHNode", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<String> getETHNode(
            @RequestParam(value = "network", required = false, defaultValue = "mainnet") String network // 网络类型: 主网或测试网
    ) {
        Assertions.assertThat(network).withFailMessage("`network` 必传").isNotBlank();
        return R.ok(web3JFactory.getEthNode(network));
    }
    
    // 根据两个ERC20代币合约地址 通过 UniSwapV2 工厂合约查询交易对合约地址
    @RequestMapping(value = "/getPairAddr", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<String> getPairAddress(
            @RequestParam(value = "network", required = false, defaultValue = "mainnet") String network, // 网络类型: 主网或测试网
            @RequestParam(value = "uniswapV2FactoryAddr", required = false, defaultValue = "0x5C69bEe701ef814a2B6a3EDD4B1652CB9cc5aA6f") String uniswapV2FactoryAddress, // uniswap v2 工厂合约地址
            @RequestParam(value = "tokenA") String tokenA, // 0x6b175474e89094c44da98b954eedeac495271d0f
            @RequestParam(value = "tokenB") String tokenB // 0xae17f4f5ca32f77ea8e3786db7c0b2fe877ac176
    ) {
        // https://etherscan.io/address/0x5C69bEe701ef814a2B6a3EDD4B1652CB9cc5aA6f#events
        // https://docs.google.com/spreadsheets/d/1jKEhOi9gIcM9bKdn7rgJEK0RKpzbE1k6bPy_kJW75Aw/edit#gid=1707981752
        Assertions.assertThat(network).withFailMessage("`network` 必传").isNotBlank();
        Web3j web3j = new Web3jOfInstanceFactory(web3JFactory.getEthNode(network)).j3();
        String pairAddress = new RemoteCallFunction<>(new UniswapV2FactoryContract(uniswapV2FactoryAddress, web3j).getPairAddress(tokenA, tokenB)).call();
        if (pairAddress.equalsIgnoreCase(Constrants.EMPTY_ADDRESS)) {
            return R.ok(null);
        }
        return R.ok(pairAddress);
    }

}
