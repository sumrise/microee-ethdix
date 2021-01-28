package com.microee.ethdix.web.actions;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.microee.ethdix.rmi.ETHBalanceRMi;
import com.microee.ethdix.rmi.ETHEstimateGasRMi;
import com.microee.ethdix.rmi.WalletRMi;

@RestController
@RequestMapping("/")
public class DefaultRestful {

    @Autowired
    private ETHBalanceRMi ethBalanceRMi;

    @Autowired
    private ETHEstimateGasRMi ethEstimateGasRMi;

    @Autowired
    private WalletRMi walletRMi;

}
