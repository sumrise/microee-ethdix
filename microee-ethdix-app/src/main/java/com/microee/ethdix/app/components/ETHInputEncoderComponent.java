package com.microee.ethdix.app.components;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class ETHInputEncoderComponent {
    
    // erc20 智能合约各方法对应的签名编码
    /* 
    * transfer(address,uint256)： 0xa9059cbb
    * balanceOf(address)：0x70a08231
    * decimals()：0x313ce567
    * allowance(address,address)： 0xdd62ed3e
    * symbol()：0x95d89b41
    * totalSupply()：0x18160ddd
    * name()：0x06fdde03
    * approve(address,uint256)：0x095ea7b3
    * transferFrom(address,address,uint256)： 0x23b872dd
     */
    public static final String TRANSFER_FUNCTION_PREFIX_METHOD_ID = "0xa9059cbb";
    public static final String APPROVE_FUNCTION_PREFIX_METHOD_ID = "0x095ea7b3";
    
    // 返回 erc20 合约转帐 input 参数
    public String getTransferInputData(String toAddress, Long amount) {
        return String.format("%s%s%s", TRANSFER_FUNCTION_PREFIX_METHOD_ID, StringUtils.leftPad(toAddress, 64, "0"), StringUtils.leftPad(Long.toHexString(amount), 64, "0"));
    }
    
    public static void main(String[] args) {
        System.out.println(StringUtils.leftPad(Long.toHexString(61795), 64, "0"));
    }
    
}
