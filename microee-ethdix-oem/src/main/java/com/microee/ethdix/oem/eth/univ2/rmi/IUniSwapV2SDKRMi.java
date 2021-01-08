package com.microee.ethdix.oem.eth.univ2.rmi;

import com.microee.plugin.response.R;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

// UniSwapV2 SDK RMi
public interface IUniSwapV2SDKRMi {
    
    @RequestMapping(value = "/token", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<String> token();
    
    @RequestMapping(value = "/pair", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<String> pair();
    
    @RequestMapping(value = "/route", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<String> route();
    
    @RequestMapping(value = "/trade", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<String> trade();
    
    @RequestMapping(value = "/fractions", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<String> fractions();
    
    @RequestMapping(value = "/fetcher", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<String> fetcher();
    
}
