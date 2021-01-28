package com.microee.ethdix.interfaces;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import com.microee.plugin.response.R;

public interface IWalletRMi {

    // 创建钱包, 返回钱包地址
    @RequestMapping(value = "/setup", method = RequestMethod.POST, consumes=MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<String[]> setup(
            @RequestParam(value = "seedCode", required = true) String seedCode,
            @RequestParam(value = "passwd", required = true) String passwd);

    // 创建钱包, 返回钱包地址
    @RequestMapping(value = "/seedCode", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<String> seedCode(
            @RequestParam(value = "wordcount", required = false, defaultValue = "12") Integer wordcount);

    // 批量生成钱包地址
    @RequestMapping(value = "/generator", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<Long> generator(
            @RequestParam(value = "count", required = false, defaultValue = "1") Integer count);
}
