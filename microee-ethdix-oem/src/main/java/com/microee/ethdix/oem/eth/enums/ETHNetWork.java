package com.microee.ethdix.oem.eth.enums;

import java.util.Arrays;

public enum ETHNetWork {

    MAINNET("mainnet", "以太坊主网"),
    TESTNET("testnet", "以太坊测试网");

    public String name;
    public String desc;

    ETHNetWork(String name, String desc) {
        this.name = name;
        this.desc = desc;
    }

    public static ETHNetWork get(final String code) {
        if (code == null || code.trim().isEmpty()) return null;
        return Arrays.asList(ETHNetWork.values()).stream()
                .filter(p -> p.name.equals(code)).findFirst().orElse(null);
    }
}
