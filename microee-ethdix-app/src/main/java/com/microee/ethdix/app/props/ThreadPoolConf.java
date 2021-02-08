package com.microee.ethdix.app.props;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import com.microee.plugin.thread.ThreadPoolConfig;

@Configuration
@ConfigurationProperties(prefix = "thread-pool-conf")
public class ThreadPoolConf {

    private Config ethMainnet;
    private Config ethRopsten;
    private Config hecoMainnet;
    
    public ThreadPoolConf() {
        super();
    }

    public Config getEthMainnet() {
        return ethMainnet;
    }

    public void setEthMainnet(Config ethMainnet) {
        this.ethMainnet = ethMainnet;
    }

    public Config getEthRopsten() {
        return ethRopsten;
    }

    public void setEthRopsten(Config ethRopsten) {
        this.ethRopsten = ethRopsten;
    }

    public Config getHecoMainnet() {
        return hecoMainnet;
    }

    public void setHecoMainnet(Config hecoMainnet) {
        this.hecoMainnet = hecoMainnet;
    }

    public static class Config extends ThreadPoolConfig {
        
        public Config() {
            super();
        }
        
    }
    
}
