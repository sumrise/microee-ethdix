package com.microee.ethdix.app.actions;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.web3j.crypto.Keys;
import com.microee.ethdix.app.components.ETHContractAddressConf;
import com.microee.ethdix.app.components.Web3JFactory;
import com.microee.ethdix.j3.contract.ERC20ContractQuery;
import com.microee.ethdix.j3.contract.RemoteCallFunction;
import com.microee.ethdix.oem.eth.enums.ChainId;
import com.microee.plugin.response.R;

//@formatter:off
/*
 * 合约名 		合约描述	 		 合约官网				
 * compound		Compound		 http://app.compound.com/					
 * uniswapv2            UniSwap V2               https://app.uniswap.org/#/swap				
 * maker		Maker			 https://oasis.app/borrow					
 * aave			Aave			 https://app.aave.com/						
 * sushiswap            SushiSwap                https://sushiswap.fi/						
 * curve		Curve			 https://www.curve.fi/						
 * yearn		Yearn			 https://yearn.finance/vaults				
 * balancer		Balancer		 https://pools.balancer.exchange/#/explore	
 * barnbridge           BarnBridge               https://app.barnbridge.com/pools			
 * synthetix            Synthetix                https://www.synthetix.io/					
 * dforce		dForce			 https://staking.dforce.network/				
 * mooniswap            Mooniswap                https://mooniswap.info/home					
 * yfii			YFII			 https://dfi.money/#/						
 * bancor		Bancor			 https://app.bancor.network/					
 * dodo			Dodo			 https://app.dodoex.io						
 * esd			Empty Set Dollar         https://emptyset.finance					
 * cream		CREAM			 https://app.cream.finance					
 * dydx			dydx			 https://dydx.exchange						
 * hegic		HEGIC			 https://www.hegic.co						
 * idle			IDLE			 https://idle.finance						
 * badger		Badger			 https://badger.finance						
 * dusd			Dusd			 https://app.dusd.finance					
 */
//@formatter:on
@RestController
@RequestMapping("/contracts")
public class ERC20QueryRestful {

    @Autowired
    private Web3JFactory web3JFactory;

    @Autowired
    private ETHContractAddressConf contractAddressConf;

    // ### 查询合约代码
    @NotNull
    @RequestMapping(value = "/eth-getCode", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<String> ethGetCode(
            @RequestParam(value = "ethnode", required = false) String ethnode,
            @RequestParam(value = "chainId", required = false, defaultValue = "mainnet") String chainId,
            @RequestParam(value = "address") String address) {
        Assertions.assertThat(ChainId.get(chainId)).withFailMessage("%s 有误", "chainId").isNotNull();
        Assertions.assertThat(Keys.toChecksumAddress(address).equalsIgnoreCase(address)).withFailMessage("%s 必传", "address").isTrue();
        return R.ok(web3JFactory.getJsonRpc(ChainId.get(chainId), ethnode).getCode(address));
    }

    // ### 查询合约基本信息
    @NotNull
    @RequestMapping(value = "/query", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<Map<String, Object>> query(
            @RequestParam(value = "ethnode", required = false) String ethnode, // 以太坊节点地址
            @RequestParam(value = "chainId", required = false, defaultValue = "mainnet") String chainId, // 网络类型: 主网或测试网
            @RequestParam(value = "address", required = false) String address, // 合约地址
            @RequestParam(value = "symbol", required = false) String symbol, // 合约币种名称
            @RequestParam(value = "attrs") String[] attrs // 合约属性字段数组
    ) { 
        Assertions.assertThat(ChainId.get(chainId)).withFailMessage("%s 有误", "chainId").isNotNull();
        Assertions.assertThat((address == null || address.isEmpty()) && (symbol == null || symbol.isEmpty())).withFailMessage("symbol OR address 二选1").isFalse();
        Assertions.assertThat(attrs).withFailMessage("%s 必传", "attrs").isNotEmpty();
        if (address == null || address.isEmpty()) {
            address = contractAddressConf.getContractAddress(ChainId.get(chainId), symbol);
        }
        Map<String, Object> map = new HashMap<>();
        for (String attr : attrs) {
            if (attr.equalsIgnoreCase("name")) {
                map.put(attr, RemoteCallFunction.build(new ERC20ContractQuery(address, web3JFactory.get(ChainId.get(chainId), ethnode)).name()).call());
            }
            if (attr.equalsIgnoreCase("symbol")) {
                map.put(attr, RemoteCallFunction.build(new ERC20ContractQuery(address, web3JFactory.get(ChainId.get(chainId), ethnode)).symbol()).call());
            }
            if (attr.equalsIgnoreCase("decimals")) {
                map.put(attr, (BigInteger) RemoteCallFunction.build(new ERC20ContractQuery(address, web3JFactory.get(ChainId.get(chainId), ethnode)).decimals()).call());
            }
            if (attr.equalsIgnoreCase("totalSupply")) {
                map.put(attr, RemoteCallFunction.build(new ERC20ContractQuery(address, web3JFactory.get(ChainId.get(chainId), ethnode)).totalSupply()).call());
            }
        }
        return R.ok(map);
    }

}
