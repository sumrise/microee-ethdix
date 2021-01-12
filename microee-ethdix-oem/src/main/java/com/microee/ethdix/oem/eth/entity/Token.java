package com.microee.ethdix.oem.eth.entity;

import java.io.Serializable;

public class Token implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private String name;
    private String address;
    private String symbol;
    private short decimals;
    private short chainId;
    private String logoURI;
    
    public Token() {
        
    }

    public Token(String name, String address, String symbol, short decimals, short chainId,
            String logoURI) {
        super();
        this.name = name;
        this.address = address;
        this.symbol = symbol;
        this.decimals = decimals;
        this.chainId = chainId;
        this.logoURI = logoURI;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public short getDecimals() {
        return decimals;
    }

    public void setDecimals(short decimals) {
        this.decimals = decimals;
    }

    public short getChainId() {
        return chainId;
    }

    public void setChainId(short chainId) {
        this.chainId = chainId;
    }

    public String getLogoURI() {
        return logoURI;
    }

    public void setLogoURI(String logoURI) {
        this.logoURI = logoURI;
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
    }
    
}
