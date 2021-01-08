package com.microee.ethdix.app.actions;

import com.microee.ethdix.app.service.ETHUniSwapV2SDKService;
import com.microee.plugin.response.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/univ2")
public class ETHUniSwapV2SDKRestful {
    
    @Autowired
    private ETHUniSwapV2SDKService univ2SDKService;
    
    @RequestMapping(value = "/token", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<String> token() {
        return R.ok(univ2SDKService.token());
    }
    
    @RequestMapping(value = "/pair", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<String> pair() {
        return R.ok(univ2SDKService.pair());
    }
    
    @RequestMapping(value = "/route", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<String> route() {
        return R.ok(univ2SDKService.route());
    }
    
    @RequestMapping(value = "/trade", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<String> trade() {
        return R.ok(univ2SDKService.trade());
    }
    
    @RequestMapping(value = "/fractions", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<String> fractions() {
        return R.ok(univ2SDKService.fractions());
    }
    
    @RequestMapping(value = "/fetcher", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<String> fetcher() {
        return R.ok(univ2SDKService.fetcher());
    }
    
}
