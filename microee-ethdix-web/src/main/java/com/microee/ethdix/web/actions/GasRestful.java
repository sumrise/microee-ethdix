package com.microee.ethdix.web.actions;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import com.microee.ethdix.oem.eth.entity.GasPriceNow;
import com.microee.ethdix.rmi.ETHEstimateGasRMi;
import com.microee.plugin.response.R;

// gas
@RestController
@RequestMapping("/gas")
public class GasRestful {

    @Autowired
    private ETHEstimateGasRMi ethEstimateGasRMi;
    
    // #### Estimate 
    // 查询四种 gas 费级别
    @RequestMapping(value = "/now-gasPricing", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<GasPriceNow> gasPricing() {
        return ethEstimateGasRMi.gasPricing();
    }
    
}
