package com.microee.ethdix.oem.eth.contract;

import java.io.Serializable;

// 符合erc20标准的合约信息
public class ERC20ContractInfo implements Serializable {

    private static final long serialVersionUID = 2819267729646572092L;

    private String address;
    private String name; // 代币名字
    private String symbol; // 代币符号
    private Integer decimals; // 精度
    private Double price;
    
    public ERC20ContractInfo() {
        
    }
    
    public ERC20ContractInfo(String address) {
        this.address = address;
    }

    public ERC20ContractInfo(String address, String name, String symbol, Integer decimals, Double price) {
        super();
        this.address = address;
        this.name = name;
        this.symbol = symbol;
        this.decimals = decimals;
        this.price = price;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public Integer getDecimals() {
        return decimals;
    }

    public void setDecimals(Integer decimals) {
        this.decimals = decimals;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

}
