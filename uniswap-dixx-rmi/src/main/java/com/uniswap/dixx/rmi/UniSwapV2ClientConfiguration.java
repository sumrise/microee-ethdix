package com.uniswap.dixx.rmi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
    public RequestInterceptor bearerHeaderAuthRequestInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(feign.RequestTemplate template) {
                LOGGER.info("feign request prepared: serviceName={} url={}{}", "uniswap-dixx", listOfServers, template.url());
            }
        };
    }
}
