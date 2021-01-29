package com.microee.ethdix.app.actions.univ2;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.web3j.abi.datatypes.Address;
import org.web3j.protocol.Web3j;
import com.microee.ethdix.app.components.Web3JFactory;
import com.microee.ethdix.app.service.ETHUniSwapV2SDKService;
import com.microee.ethdix.interfaces.univ2.IETHUniSwapV2RMi;
import com.microee.ethdix.j3.Constrants;
import com.microee.ethdix.j3.contract.RemoteCallFunction;
import com.microee.ethdix.j3.factory.Web3jOfInstanceFactory;
import com.microee.ethdix.j3.uniswap.UniswapV2FactoryContract;
import com.microee.ethdix.j3.uniswap.UniswapV2Route02Contract;
import com.microee.ethdix.oem.eth.entity.Token;
import com.microee.ethdix.oem.eth.enums.ChainId;
import com.microee.plugin.response.R;

// #### 在V2中
// 不再将ETH做为中间币, 只需调用路由合约上的 swapExactTokensForTokens 和 swapTokensForExactTokens 方法即可交易
// 上述方法名中的 Exact 表示你希望为交换中的哪一种代币设置限额。
// 如果你想用 DAI 买入一定数量的 ETH，就需要使用 swapTokensForExactTokens
// 另一方面如果你想用一定数量的 DAI 买入 ETH，就需要使用 swapExactTokensForTokens。
// UniSwapV2的智能和鱼就是采用了这种方法
// https://uniswap.org
@RestController
@RequestMapping("/univ2")
public class ETHUniSwapV2Restful implements IETHUniSwapV2RMi {

    @Autowired
    private Web3JFactory web3JFactory;

    @Autowired
    private ETHUniSwapV2SDKService uniSwapV2SDKService;

    /**
     * 查询默认支持的 token 列表
     * @param chainId
     * @return
     */
    @Override
    @RequestMapping(value = "/default-token-list", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<List<Token>> defaultTokenList(
            @RequestParam(value = "chainId", required=false, defaultValue="mainnet") String chainId,
            @RequestParam(value = "symbol", required=false) String symbol) {
        Assertions.assertThat(ChainId.get(chainId)).withFailMessage("`chainId`有误").isNotNull();
        return R.ok(uniSwapV2SDKService.defaultTokenList(ChainId.get(chainId).code, symbol));
    }
    
    // 根据 router02 合约地址查询相关信息, 例如: UniSwapV2工厂合约地址
    // https://uniswap.org/docs/v2/smart-contracts/router02/
    @RequestMapping(value = "/router02", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<Map<String, Object>> router02(
            @RequestParam(value = "chainId", required = false, defaultValue = "mainnet") String chainId, // 网络类型: 主网或测试网
            @RequestParam(value = "router02Addr", required = false, defaultValue = "0x7a250d5630B4cF539739dF2C5dAcb4c659F2488D") String router02Addr,
            @RequestParam(value = "attrs", required = false) String[] attrs // 合约属性字段数组
    ) {
        Assertions.assertThat(ChainId.get(chainId)).withFailMessage("%s 有误", "chainId").isNotNull();
        Map<String, Object> map = new HashMap<>();
        if (attrs != null) {
            for (String attr : attrs) {
                if (attr.equalsIgnoreCase("factory")) {
                    Address factoryAddr = RemoteCallFunction.build(new UniswapV2Route02Contract(router02Addr, web3JFactory.get(ChainId.get(chainId))).factoryAddr()).call();
                    map.put(attr, factoryAddr == null ? null : factoryAddr.getValue());
                }
                if (attr.equalsIgnoreCase("WETH")) {
                    Address wethAddr = RemoteCallFunction.build(new UniswapV2Route02Contract(router02Addr, web3JFactory.get(ChainId.get(chainId))).WETH()).call();
                    map.put(attr, wethAddr == null ? null : wethAddr.getValue());
                }
            }
        }
        return R.ok(map);
    }
    
    // 根据两个ERC20代币合约地址 通过 UniSwapV2 工厂合约查询交易对合约地址
    @RequestMapping(value = "/getPairAddr", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<String> getPairAddress(
            @RequestParam(value = "chainId", required = false, defaultValue = "mainnet") String chainId, // 网络类型: 主网或测试网
            @RequestParam(value = "univ2FactoryAddr", required = false, defaultValue = "0x5C69bEe701ef814a2B6a3EDD4B1652CB9cc5aA6f") String univ2FactoryAddr, // uniswap v2 工厂合约地址
            @RequestParam(value = "tokenA") String tokenA, // 0x6b175474e89094c44da98b954eedeac495271d0f
            @RequestParam(value = "tokenB") String tokenB // 0xae17f4f5ca32f77ea8e3786db7c0b2fe877ac176
    ) {
        // https://etherscan.io/address/0x5C69bEe701ef814a2B6a3EDD4B1652CB9cc5aA6f#events
        // https://docs.google.com/spreadsheets/d/1jKEhOi9gIcM9bKdn7rgJEK0RKpzbE1k6bPy_kJW75Aw/edit#gid=1707981752
        Assertions.assertThat(ChainId.get(chainId)).withFailMessage("%s 有误", "chainId").isNotNull();
        Web3j web3j = new Web3jOfInstanceFactory(web3JFactory.getEthNode(ChainId.get(chainId))).j3();
        String pairAddress = new RemoteCallFunction<>(new UniswapV2FactoryContract(univ2FactoryAddr, web3j).getPairAddress(tokenA, tokenB)).call();
        if (pairAddress.equalsIgnoreCase(Constrants.EMPTY_ADDRESS)) {
            return R.ok(null);
        }
        return R.ok(pairAddress);
    }
    
    // 查询代币交换合约地址
    @RequestMapping(value = "/getExchangeAddr", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<String> getExchangeAddr(
            @RequestParam(value = "chainId", required = false, defaultValue = "mainnet") String chainId, // 网络类型: 主网或测试网
            @RequestParam(value = "univ2FactoryAddr", required = false, defaultValue = "0x5C69bEe701ef814a2B6a3EDD4B1652CB9cc5aA6f") String univ2FactoryAddr,
            @RequestParam(value = "tokenAddr", required = true) String tokenAddr
    ) {
        Assertions.assertThat(ChainId.get(chainId)).withFailMessage("%s 有误", "chainId").isNotNull();
        Assertions.assertThat(tokenAddr).withFailMessage("`tokenAddr` 必传").isNotBlank();
        String exchangeAddr = new RemoteCallFunction<>(new UniswapV2FactoryContract(univ2FactoryAddr, web3JFactory.get(ChainId.get(chainId))).getExchangeAddr(tokenAddr)).call();
        return R.ok(Constrants.EMPTY_ADDRESS.equalsIgnoreCase(exchangeAddr) ? null : exchangeAddr);
    }

    // v2的工厂合约貌似不支持通过交换合约地址查token地址, 所以此处传的是v1的工厂合约地址
    // 根据交换合约地址查询代币地址
    @RequestMapping(value = "/getTokenAddress", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<String> getTokenAddress(
            @RequestParam(value = "chainId", required = false, defaultValue = "mainnet") String chainId, // 网络类型: 主网或测试网
            @RequestParam(value = "univ1FactoryAddr", required = false, defaultValue = "0xc0a47dFe034B400B47bDaD5FecDa2621de6c4d95") String univ1FactoryAddr,
            @RequestParam(value = "exchangeAddr", required = true) String exchangeAddr
    ) {
        Assertions.assertThat(ChainId.get(chainId)).withFailMessage("%s 有误", "chainId").isNotNull();
        Assertions.assertThat(exchangeAddr).withFailMessage("`exchangeAddr` 必传").isNotBlank();
        String tokenAddr = new RemoteCallFunction<>(new UniswapV2FactoryContract(univ1FactoryAddr, web3JFactory.get(ChainId.get(chainId))).getToken(exchangeAddr)).call();
        return R.ok(Constrants.EMPTY_ADDRESS.equalsIgnoreCase(tokenAddr) ? null : tokenAddr);
    }
    
}
