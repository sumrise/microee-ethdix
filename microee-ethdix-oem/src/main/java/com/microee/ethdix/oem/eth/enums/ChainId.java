package com.microee.ethdix.oem.eth.enums;

import java.util.Arrays;

// 以太坊节点网络类型
public enum ChainId {

    MAINNET((short)1, "mainnet"),
    ROPSTEN((short)3, "ropsten"),
    RINKEBY((short)4, "rinkeby"),
    GÖRLI((short)5, "görli"),
    KOVAN((short)42, "kovan"),
    HECO((short)128, "heco");

    public short code;
    public String name;

    ChainId(short code, String name) {
        this.code = code;
        this.name = name;
    }

    public static ChainId get(final String code) {
        if (code == null || code.trim().isEmpty()) return null;
        return Arrays.asList(ChainId.values()).stream()
                .filter(p -> p.name.equalsIgnoreCase(code) || String.valueOf(p.code).equals(code) ).findFirst().orElse(null);
    }
    
}
