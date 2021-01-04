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

    private ConcurrentMap<String, Web3j> map = new ConcurrentHashMap<>();

    public Web3j get(String network) {
        return get(network, null);
    }

    public Web3j get(String network, String ethnode) {
        if (ethnode != null) {
            return build(ethnode);
        }
        return build(this.getEthNode(network));
    }

    private synchronized Web3j build(String ethnode) {
        if (map.containsKey(ethnode.toLowerCase())) {
            return map.get(ethnode.toLowerCase());
        }
        map.put(ethnode.toLowerCase(), new Web3jOfInstanceFactory(ethnode).j3());
        return map.get(ethnode.toLowerCase());
    }

    public String getEthNode(String network) {
        if (network.equals("mainnet")) {
            return UsedCount.getEthNode(ethNetworkProperties.mainnet());
        }
        return UsedCount.getEthNode(ethNetworkProperties.ropsten());
    }

    public JsonRPC getJsonRpc(String network) {
        if (network.equals("mainnet")) {
            return jsonRPCClientMainnet;
        }
        return jsonRPCClientRopsten;
    }

}
