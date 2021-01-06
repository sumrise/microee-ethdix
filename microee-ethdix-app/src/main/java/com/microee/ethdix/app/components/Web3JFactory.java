package com.microee.ethdix.app.components;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.web3j.protocol.Web3j;
import com.microee.ethdix.app.props.ETHNetworkProperties;
import com.microee.ethdix.j3.factory.Web3jOfInstanceFactory;
import com.microee.ethdix.j3.rpc.JsonRPC;
import com.microee.ethdix.j3.rpc.JsonRPC.UsedCount;
import com.microee.plugin.response.R;
import com.microee.plugin.response.exception.RestException;

@Component
public class Web3JFactory {

    @Autowired
    private ETHNetworkProperties ethNetworkProperties;

    @Autowired
    @Qualifier("jsonRPCClientMainnet")
    private JsonRPC jsonRPCClientMainnet;

    @Autowired
    @Qualifier("jsonRPCClientRopsten")
    private JsonRPC jsonRPCClientRopsten;

    private final ConcurrentMap<String, Web3j> map = new ConcurrentHashMap<>();

    // mainnet, Ropsten, Rinkeby, Görli, Kovan
    public Web3j get(String network) {
        if (network.startsWith("http")) {
            throw new RestException(R.FAILED, "非法参数,误把节点地址当成网络类型传递!");
        }
        return get(network, null);
    }

    public Web3j get(String network, String ethnode) {
        if (ethnode != null) {
            return build(ethnode);
        }
        return build(this.getEthNode(network));
    }

    public Web3j getByEthNode(String ethnode) {
        return build(ethnode);
    }

    private synchronized Web3j build(String ethnode) {
        if (map.containsKey(ethnode.toLowerCase())) {
            return map.get(ethnode.toLowerCase());
        }
        map.put(ethnode.toLowerCase(), new Web3jOfInstanceFactory(ethnode).j3());
        return map.get(ethnode.toLowerCase());
    }

    public String getEthNode(String network) {
        if (network.startsWith("http")) {
            throw new RestException(R.FAILED, "非法参数,误把节点地址当成网络类型传递!");
        }
        if (network.equals("mainnet")) {
            return UsedCount.getEthNode(ethNetworkProperties.mainnet());
        }
        return UsedCount.getEthNode(ethNetworkProperties.ropsten());
    }

    public JsonRPC getJsonRpcByEthNode(String ethnode) {
        return new JsonRPC(ethnode);
    }

    public JsonRPC getJsonRpc(String network) {
        return this.getJsonRpc(network, null);
    }

    public JsonRPC getJsonRpc(String network, String ethnode) {
        if (ethnode != null) {
            return new JsonRPC(ethnode);
        }
        if (network.startsWith("http")) {
            throw new RestException(R.FAILED, "非法参数,误把节点地址当成网络类型传递!");
        }
        if (network.equals("mainnet")) {
            return jsonRPCClientMainnet;
        }
        return jsonRPCClientRopsten;
    }

}
