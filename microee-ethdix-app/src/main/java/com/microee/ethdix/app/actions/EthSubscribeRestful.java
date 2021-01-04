package com.microee.ethdix.app.actions;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.microee.ethdix.j3.rpc.JsonRPC;
import com.microee.plugin.response.R;

@RestController
@RequestMapping("/subscribe")
public class EthSubscribeRestful {

    @Autowired
    @Qualifier("jsonRPCClientMainnet")
    private JsonRPC jsonRPCClientMainnet;

    @Autowired
    @Qualifier("jsonRPCClientRopsten")
    private JsonRPC jsonRPCClientRopsten;
    
    // ### 订阅新加入的区块
    @RequestMapping(value = "/newHeads", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<Boolean> newHeads(@RequestParam(value = "network", required=false, defaultValue="mainnet") String network) {
        String params = String.format("{\"jsonrpc\":\"2.0\", \"id\": \"newHeads\", \"method\": \"eth_subscribe\", \"params\": [\"%s\"]}", "newHeads");
        if (network.equals("mainnet")) {
            return R.ok(jsonRPCClientMainnet.subscribe(params));
        }
        return R.ok(jsonRPCClientRopsten.subscribe(params));
    }

    // ### 订阅新订阅指定地址的事件
    @RequestMapping(value = "/logs", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<Boolean> logs(@RequestParam("address") String address, @RequestParam("topic") String topic) {
        String params = String.format("{\"jsonrpc\":\"2.0\", \"id\": \"logs\", \"method\": \"eth_subscribe\", \"params\": [\"logs\", {\"address\": \"%s\", \"topics\": [\"%s\"]}]}", address, topic);
        return R.ok(jsonRPCClientMainnet.subscribe(params));
    }

    // ### 订阅新加入的交易
    @RequestMapping(value = "/newPendingTransactions", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<Boolean> newPendingTransactions() {
        String params = String.format("{\"jsonrpc\":\"2.0\", \"id\": \"newPendingTransactions\", \"method\": \"eth_subscribe\", \"params\": [\"%s\"]}", "newPendingTransactions");
        return R.ok(jsonRPCClientMainnet.subscribe(params));
    }
    
}
