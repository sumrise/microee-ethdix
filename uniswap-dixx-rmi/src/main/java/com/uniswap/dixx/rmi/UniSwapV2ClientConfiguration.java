package com.uniswap.dixx.rmi;

import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.microee.plugin.context.AppContext;
import feign.RequestInterceptor;

/**
 * 配置头部参数
 */
@Configuration
public class UniSwapV2ClientConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(UniSwapV2ClientConfiguration.class);

    @Value("${micro.services.uniswap-dixx.listOfServers}")
    private String listOfServers;

    @Bean
    public RequestInterceptor feignRequestInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(feign.RequestTemplate template) {
                AppContext.putFeignTimestamp(Instant.now().toEpochMilli());
                LOGGER.info("feign request prepared: serviceName={} url={}{}", "uniswap-dixx", listOfServers, template.url());
            }
        };
    }
}
