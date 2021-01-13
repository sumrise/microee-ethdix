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
import com.microee.ethdix.j3.contract.ETHInputEncoder;
import com.microee.ethdix.j3.contract.RemoteCallFunction;
import com.microee.ethdix.j3.factory.Web3jOfInstanceFactory;
import com.microee.ethdix.j3.uniswap.UniswapV2FactoryContract;
import com.microee.ethdix.j3.uniswap.UniswapV2Route02Contract;
import com.microee.ethdix.oem.eth.enums.ChainId;
import com.microee.plugin.response.R;
import java.util.HashMap;
import java.util.Map;
import org.web3j.abi.datatypes.Address;

// #### 在V2中
// 不再将ETH做为中间币, 只需调用路由合约上的 swapExactTokensForTokens 和 swapTokensForExactTokens 方法即可交易
// 上述方法名中的 Exact 表示你希望为交换中的哪一种代币设置限额。
// 如果你想用 DAI 买入一定数量的 ETH，就需要使用 swapTokensForExactTokens
// 另一方面如果你想用一定数量的 DAI 买入 ETH，就需要使用 swapExactTokensForTokens。
// UniSwapV2的智能和鱼就是采用了这种方法
// https://uniswap.org
@RestController
@RequestMapping("/univ2")
public class ETHUniSwapV2Restful {

    @Autowired
    private Web3JFactory web3JFactory;
    
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

    
    // ETH与代币兑换, eth减少, 代币增加
    @RequestMapping(value = "/eth2TokenSwap", method = RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<String> eth2TokenSwap(
            @RequestParam(value = "ethnode", required = false) String ethnode, // 以太坊节点地址
            @RequestParam(value = "router02Addr", required = false, defaultValue = "0x7a250d5630B4cF539739dF2C5dAcb4c659F2488D") String router02Addr,
            @RequestParam(value = "contractAddr", required = true) String contractAddr, // erc20 合约地址
            @RequestParam(value = "toAddr", required = true) String toAddr, // 转入地址, 账户地址
            @RequestParam(value = "gasPrice", required = true) Long gasPrice, // 即 rapid OR fast OR standard OR slow 对应的值
            @RequestParam(value = "gasLimit", required = false, defaultValue = "21000") Long gasLimit,
            @RequestParam(value = "amount", required = true) Double amount, // 转帐数量, 单位是 erc20代币单位
            @RequestParam(value = "fromAddress", required = true) String fromAddress, // 出帐地址
            @RequestParam(value = "privateKey", required = true) String privateKey // 出帐地址
    ) {
        // We use this slippage tolerance to calculate the minumum amount of DAI we must receive before our trade reverts, thanks to minimumAmountOut. 
        // The value is the amount of ETH that must be included as the msg.value in our transaction.
        // const amountOutMin = trade.minimumAmountOut(slippageTolerance).raw
        Double slippageTolerance = 0.2d;
        Long amountOutMin = null;//(amount * slippageTolerance);
        Address wethAddr = RemoteCallFunction.build(new UniswapV2Route02Contract(router02Addr, web3JFactory.getByEthNode(ethnode)).WETH()).call();
        String inputData = ETHInputEncoder.getInputDataForSwapExactETHForTokens(amountOutMin, wethAddr.getValue(), contractAddr, toAddr);
        return R.ok(inputData);
    }
    
}
