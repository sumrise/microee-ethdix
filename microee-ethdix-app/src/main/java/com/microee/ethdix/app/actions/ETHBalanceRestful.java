package com.microee.ethdix.app.actions;

import java.math.BigDecimal;
import org.assertj.core.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.microee.ethdix.app.components.ETHContractAddressConf;
import com.microee.ethdix.app.components.Web3JFactory;
import com.microee.ethdix.interfaces.IETHBalanceRMi;
import com.microee.ethdix.j3.contract.ERC20ContractQuery;
import com.microee.ethdix.j3.rpc.JsonRPC;
import com.microee.ethdix.oem.eth.enums.ChainId;
import com.microee.plugin.response.R;

@RestController
@RequestMapping("/balance")
public class ETHBalanceRestful implements IETHBalanceRMi {

    @Autowired
    private Web3JFactory web3JFactory;

    @Autowired
    private ETHContractAddressConf contractAddressConf;

    /**
     * 查询余额
     *
     * @param username
     * @param password
     * @param ethnode
     * @param network
     * @param currency
     * @param accountAddress
     * @param contractAddress 例如: usdt 主网合约地址
     * 0xdAC17F958D2ee523a2206206994597C13D831ec7
     * @param userAddress 例如: 一个用户地址 0x493ba3316c2e246d55edf81427d833631eff9a10
     * @return
     * @throws Exception
     */
    @Override
    @RequestMapping(value = "/query", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<Double> balanceOf(
            @RequestHeader(value = "username", required = false) String username,
            @RequestHeader(value = "password", required = false) String password,
            @RequestParam(value = "ethnode", required = false) String ethnode, // 以太坊节点地址
            @RequestParam(value = "chainId", required = false, defaultValue = "mainnet") String chainId, // 网络类型: 主网或测试网
            @RequestParam(value = "currency") String currency, // 币种
            @RequestParam(value = "accountAddress") String accountAddress
    ) throws Exception {
        Assertions.assertThat(ChainId.get(chainId)).withFailMessage("%s 有误", "chainId").isNotNull();
        Assertions.assertThat(currency).withFailMessage("%s 必传", "currency").isNotBlank();
        Assertions.assertThat(accountAddress).withFailMessage("%s 必传", "accountAddress").isNotBlank();
        if (currency.equalsIgnoreCase("eth")) {
            JsonRPC jsonRpc = ethnode != null && username != null && password != null ? new JsonRPC(ChainId.get(chainId), ethnode, username, password) : web3JFactory.getJsonRpc(ChainId.get(chainId));
            Long balance = jsonRpc.getQueryEthBalance(accountAddress);
            return R.ok( balance == 0 ? 0.0 : BigDecimal.valueOf(balance).divide(new BigDecimal("10").pow(18)).doubleValue());
        }
        String contractAddress = contractAddressConf.getContractAddress(ChainId.get(chainId), currency);
        ERC20ContractQuery erc20Contract = new ERC20ContractQuery(contractAddress, web3JFactory.get(ChainId.get(chainId), ethnode));
        Long balance = erc20Contract.balanceOf(accountAddress).send().longValue();
        return R.ok(balance == 0 ? 0.0 : BigDecimal.valueOf(balance).divide(new BigDecimal("10").pow(erc20Contract.decimals().send().intValue())).doubleValue());
    }

}
