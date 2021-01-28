package com.microee.ethdix.app.actions;

import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.microee.ethdix.interfaces.IWalletRMi;
import com.microee.ethdix.j3.address.DeterministicKeys;
import com.microee.plugin.response.R;

// 钱包
@RestController
@RequestMapping("/wallet")
public class WalletRestful implements IWalletRMi {

    private static final Logger LOGGER = LoggerFactory.getLogger(WalletRestful.class);
    
    // 创建钱包, 返回钱包地址
    @Override
    @RequestMapping(value = "/setup", method = RequestMethod.POST, consumes=MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<String[]> setup(
            @RequestParam(value = "seedCode", required = true) String seedCode,
            @RequestParam(value = "passwd", required = true) String passwd) {
        return R.ok(DeterministicKeys.generator(seedCode, passwd)); 
    }

    // 创建钱包, 返回钱包地址
    @Override
    @RequestMapping(value = "/seedCode", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<String> seedCode(
            @RequestParam(value = "wordcount", required = false, defaultValue = "12") Integer wordcount) {
        return R.ok(DeterministicKeys.generateNewMnemonic(wordcount));
    }

    // 批量生成钱包地址
    @Override
    @RequestMapping(value = "/generator", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<Long> generator(
            @RequestParam(value = "count", required = false, defaultValue = "1") Integer count) {
        Long start = Instant.now().toEpochMilli();
        for (int i = 0; i < count; i++) {
            String address = DeterministicKeys.generator(DeterministicKeys.generateNewMnemonic(12), "123111")[0];
            LOGGER.info("address={}", address);
        }
        return R.ok(Instant.now().toEpochMilli() - start);
    }
    
}
