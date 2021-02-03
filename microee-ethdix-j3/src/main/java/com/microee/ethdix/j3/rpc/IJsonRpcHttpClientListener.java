package com.microee.ethdix.j3.rpc;

import com.microee.ethdix.oem.eth.enums.ChainId;

public interface IJsonRpcHttpClientListener {

    public void log(ChainId chainId, boolean success, int statusCode, long contentLength, String method, String url, String message, Long start, Long speed);
}
