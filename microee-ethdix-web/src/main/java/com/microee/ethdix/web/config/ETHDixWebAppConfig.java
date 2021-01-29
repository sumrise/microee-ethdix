package com.microee.ethdix.web.config;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ETHDixWebAppConfig implements ApplicationListener<ApplicationEvent>{
    
    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof ApplicationReadyEvent) {
            
        }
    }

}
