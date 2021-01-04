package com.microee.ethdix.j3.rpc;

import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

import okhttp3.Credentials;

public class Web3J {

    static Web3j web3jOfMainnet;
    static Web3j web3jOfRopsten;

    private static final String infuraServiceURLOfMainnet = "https://mainnet.infura.io/v3/8b5de70d6cdc460e911a3bb141dc1a79"; // 以太坊主网地址
    private static final String infuraServiceURLOfRopsten = "https://ropsten.infura.io/v3/d266a83cc83a40d7b14257be4579d310"; // 以太坊测试网地址

    private static final String infuraUsername = "";
    private static final String infuraPassword = "";

    static {
        HttpService httpServiceOfMainnet = new HttpService(infuraServiceURLOfMainnet);
        httpServiceOfMainnet.addHeader("Authorization", Credentials.basic(infuraUsername, infuraPassword));
        web3jOfMainnet = Web3j.build(httpServiceOfMainnet);
        HttpService httpServiceOfRopsten = new HttpService(infuraServiceURLOfRopsten);
        httpServiceOfRopsten.addHeader("Authorization", Credentials.basic(infuraUsername, infuraPassword));
        web3jOfRopsten = Web3j.build(httpServiceOfRopsten);
    }

}
