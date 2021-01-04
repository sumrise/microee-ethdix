package com.microee.ethdix.oem.eth.enums;

import java.util.Arrays;

public enum ETHContractType {

    NAN("nan", "不是以太坊合约"), ERC20("erc20", "erc20合约"), ERC721("erc721",
            "erc721合约"), ERC1155("erc1155", "erc1155合约");

    public String name;
    public String desc;

    ETHContractType(String name, String desc) {
        this.name = name;
        this.desc = desc;
    }

    public static ETHContractType get(final String code) {
        if (code == null || code.trim().isEmpty())
            return null;
        return Arrays.asList(ETHContractType.values()).stream().filter(p -> p.name.equals(code))
                .findFirst().orElse(null);
    }
}
