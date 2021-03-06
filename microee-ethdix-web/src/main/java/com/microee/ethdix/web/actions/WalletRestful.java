package com.microee.ethdix.web.actions;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.microee.ethdix.rmi.WalletRMi;
import com.microee.plugin.response.R;

// 钱包
@RestController
@RequestMapping("/wallet")
public class WalletRestful {

    @Autowired
    private WalletRMi walletRMi;
    
    // #### Wallet
    // 创建钱包, 返回钱包地址
    @RequestMapping(value = "/setup", method = RequestMethod.POST, consumes=MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<String[]> setup(
            @RequestParam(value = "seedCode", required = true) String seedCode,
            @RequestParam(value = "passwd", required = true) String passwd) {
        return walletRMi.setup(seedCode, passwd);
    }

    // 创建钱包, 返回钱包地址
    @RequestMapping(value = "/seedCode", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<String> seedCode(
            @RequestParam(value = "wordcount", required = false, defaultValue = "12") Integer wordcount) {
        return walletRMi.seedCode(wordcount);
    }
    
}
