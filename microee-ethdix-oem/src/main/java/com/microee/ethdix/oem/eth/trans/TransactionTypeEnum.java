package com.microee.ethdix.oem.eth.trans;

// 交易类型枚举
public enum TransactionTypeEnum {
    /**
     * 转账
     */
    TRANSFER,
    /**
     * 收款
     */
    RECEIPT,
    /**
     * 授权
     */
    APPROVE,
    /**
     * 交易
     */
    SWAP,
    /**
     * 执行合约
     */
    CONTRACT
}
