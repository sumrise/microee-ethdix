package com.microee.ethdix.interfaces;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import com.microee.plugin.response.R;

public interface IETHBalanceRMi {

    @RequestMapping(value = "/query", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<Double> balanceOf(
            @RequestHeader(value = "username", required = false) String username,
            @RequestHeader(value = "password", required = false) String password,
            @RequestParam(value = "ethnode", required = false) String ethnode, // 以太坊节点地址
            @RequestParam(value = "chainId", required = false, defaultValue = "mainnet") String chainId, // 网络类型: 主网或测试网
            @RequestParam(value = "currency") String currency, // 币种
            @RequestParam(value = "accountAddress") String accountAddress) throws Exception;
    
}
