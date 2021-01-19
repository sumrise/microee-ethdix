package com.microee.ethdix.app.actions;

import org.assertj.core.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.microee.ethdix.app.components.Web3JFactory;
import com.microee.ethdix.oem.eth.enums.ChainId;
import com.microee.plugin.response.R;

@RestController
@RequestMapping("/subscribe")
public class ETHSubscribeRestful {

    @Autowired
    private Web3JFactory web3JFactory;
    
    // ### 订阅新加入的区块
    @RequestMapping(value = "/newHeads", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<Boolean> newHeads(
            @RequestParam(value = "chainId", required = false, defaultValue = "mainnet") String chainId) {
        Assertions.assertThat(ChainId.get(chainId)).withFailMessage("%s 有误", "chainId").isNotNull();
        String params = String.format("{\"jsonrpc\":\"2.0\", \"id\": \"newHeads\", \"method\": \"eth_subscribe\", \"params\": [\"%s\"]}", "newHeads");
        return R.ok(web3JFactory.getJsonRpc(ChainId.get(chainId)).subscribe(params));
    }

    // ### 订阅新订阅指定地址的事件
    @RequestMapping(value = "/logs", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<Boolean> logs(
            @RequestParam(value = "chainId", required = false, defaultValue = "mainnet") String chainId,
            @RequestParam("address") String address, @RequestParam("topic") String topic) {
        Assertions.assertThat(ChainId.get(chainId)).withFailMessage("%s 有误", "chainId").isNotNull();
        String params = String.format("{\"jsonrpc\":\"2.0\", \"id\": \"logs\", \"method\": \"eth_subscribe\", \"params\": [\"logs\", {\"address\": \"%s\", \"topics\": [\"%s\"]}]}", address, topic);
        return R.ok(web3JFactory.getJsonRpc(ChainId.get(chainId)).subscribe(params));
    }

    // ### 订阅新加入的交易
    @RequestMapping(value = "/newPendingTransactions", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<Boolean> newPendingTransactions(
            @RequestParam(value = "chainId", required = false, defaultValue = "mainnet") String chainId) {
        String params = String.format("{\"jsonrpc\":\"2.0\", \"id\": \"newPendingTransactions\", \"method\": \"eth_subscribe\", \"params\": [\"%s\"]}", "newPendingTransactions");
        return R.ok(web3JFactory.getJsonRpc(ChainId.get(chainId)).subscribe(params));
    }
    
}
