package com.microee.ethdix.rmi;

import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.microee.plugin.context.AppContext;
import feign.RequestInterceptor;

@Configuration
public class ETHDixRMiConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(ETHDixRMiConfiguration.class);
    private static final String serviceName = "microee-ethdix-app";

    @Bean
    public RequestInterceptor feignRequestInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(feign.RequestTemplate template) {
                AppContext.putFeignTimestamp(Instant.now().toEpochMilli());
                LOGGER.info("feign request prepared: serviceName={} url={}", serviceName, template.request().url());
            }
        };
    }
    
}
