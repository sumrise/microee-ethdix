package com.microee.ethdix.j3.factory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import okhttp3.Credentials;

public class Web3jOfInstanceFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(Web3jOfInstanceFactory.class);
    // 加上 volatile 禁止了指令的重排序操作。满足多线程下的单例，懒加载，获取实例的高效性。
    private volatile static Web3jOfInstanceFactory instance;
    private final Web3j web3jOfInstance; // 实例数据

    public Web3jOfInstanceFactory(String networkOfUrl) {
        this(networkOfUrl, null, null);
    }
    
    public Web3jOfInstanceFactory(String networkOfUrl, String username, String password) {
        HttpService httpService = new HttpService(networkOfUrl);
        if (username != null && !username.isEmpty()) {
        	httpService.addHeader("Authorization", Credentials.basic(username, password));
        }
        this.web3jOfInstance = Web3j.build(httpService);
    }

    public static Web3jOfInstanceFactory newInstance(String networkOfUrl, String username,
            String password) {
        if (instance == null) {
            synchronized (Web3jOfInstanceFactory.class) {
                if (instance == null) {
                    instance = new Web3jOfInstanceFactory(networkOfUrl, username, password);
                    LOGGER.info("创建了一个Web3j实例: networkOfUrl={}, username={}", networkOfUrl,
                            username);
                }
            }
        }
        return instance;
    }

    public Web3j j3() {
        return this.web3jOfInstance;
    }

}
