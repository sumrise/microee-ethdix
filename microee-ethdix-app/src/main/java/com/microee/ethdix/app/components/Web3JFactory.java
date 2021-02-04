package com.microee.ethdix.app.components;

import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.web3j.protocol.Web3j;
import com.microee.ethdix.app.listeners.JsonRpcHttpClientListener;
import com.microee.ethdix.app.props.ETHConfigurationProperties;
import com.microee.ethdix.j3.factory.Web3jOfInstanceFactory;
import com.microee.ethdix.j3.rpc.JsonRPC;
import com.microee.ethdix.j3.rpc.JsonRPC.UsedCount;
import com.microee.ethdix.oem.eth.enums.ChainId;
import com.microee.plugin.response.R;
import com.microee.plugin.response.exception.RestException;

@Component
public class Web3JFactory {

    @Autowired
    private ETHConfigurationProperties ethNetworkProperties;

    @Autowired
    @Qualifier("jsonRPCClientMainnet")
    private JsonRPC jsonRPCClientMainnet;

    @Autowired
    @Qualifier("jsonRPCClientRopsten")
    private JsonRPC jsonRPCClientRopsten;

    @Autowired
    @Qualifier("jsonRPCClientHecoMainnet")
    private JsonRPC jsonRPCClientHecoMainnet;

    @Autowired
    private JsonRpcHttpClientListener jsonRpcHttpListener;
    
    private final ConcurrentMap<String, Web3j> map = new ConcurrentHashMap<>();

    // mainnet, Ropsten, Rinkeby, Görli, Kovan
    public Web3j get(ChainId chainId) {
        return get(chainId, null);
    }

    public Web3j get(ChainId chainId, String ethnode) {
        if (ethnode != null) {
            return build(ethnode);
        }
        return build(this.getEthNode(chainId));
    }

    public Web3j getByEthNode(String ethnode) {
        return build(ethnode);
    }

    private synchronized Web3j build(String ethnode) {
        if (map.containsKey(ethnode.toLowerCase(Locale.getDefault()))) {
            return map.get(ethnode.toLowerCase(Locale.getDefault()));
        }
        map.put(ethnode.toLowerCase(Locale.getDefault()), new Web3jOfInstanceFactory(ethnode).j3());
        return map.get(ethnode.toLowerCase(Locale.getDefault()));
    }

    public String getEthNode(ChainId chainId) {
        if (ChainId.MAINNET.equals(chainId)) {
            return UsedCount.getEthNode(ethNetworkProperties.mainnet());
        }
        if (ChainId.ROPSTEN.equals(chainId)) {
            return UsedCount.getEthNode(ethNetworkProperties.ropsten());
        }
        throw new RestException(R.SERVER_ERROR, "不支持的链id");
    }

    public JsonRPC getJsonRpc(ChainId chainId) {
        return this.getJsonRpc(chainId, null).setHttpClientLoggerListener(jsonRpcHttpListener);
    }

    @NotNull
    public JsonRPC getJsonRpc(ChainId chainId, String ethnode) {
        if (ethnode != null) {
            return new JsonRPC(chainId, ethnode).setHttpClientLoggerListener(jsonRpcHttpListener);
        }
        if (ChainId.MAINNET.equals(chainId)) {
            return jsonRPCClientMainnet;
        }
        if (ChainId.ROPSTEN.equals(chainId)) {
            return jsonRPCClientRopsten;
        }
        if (ChainId.HECO.equals(chainId)) {
            return jsonRPCClientHecoMainnet;
        }
        throw new RestException(R.SERVER_ERROR, "不支持的链id");
    }

}
