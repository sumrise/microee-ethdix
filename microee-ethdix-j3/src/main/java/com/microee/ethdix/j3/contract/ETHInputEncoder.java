package com.microee.ethdix.j3.contract;

import java.util.Arrays;
import java.util.Collections;
import org.apache.commons.lang.StringUtils;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;

// 合约input辅助函数库
public final class ETHInputEncoder {
    
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
    
    // 0xa9059cbb0000000000000000000000009e7b1a22cf4d69efad71bac24a6a8518574f1fc40000000000000000000000000000000000000000000000000000008fe0b0b380
    // 返回 erc20 合约转帐 input 参数
    public static String getTransferInputData(String toAddress, Long amount) {
        final int len = 64;
        String theToAddress = toAddress.startsWith("0x") ? StringUtils.leftPad(toAddress.substring(2), len, "0") : StringUtils.leftPad(toAddress, len, "0");
        String theAmount = StringUtils.leftPad(Long.toHexString(amount), len, "0");
        return String.format("%s%s%s", TRANSFER_FUNCTION_PREFIX_METHOD_ID, theToAddress, theAmount);
    }
    
    // 0xa9059cbb0000000000000000000000009e7b1a22cf4d69efad71bac24a6a8518574f1fc40000000000000000000000000000000000000000000000000000008fe0b0b380
    // 获取以太坊代币转帐合约 input 参数
    public static String getInputDataForTokenTransfer(String to, Long amount) {
        // BigInteger val = amount.multiply(new BigDecimal("10").pow(decimal)).toBigInteger(); // 根据精度转成最小单位
        Function function = new Function(
                "transfer",
                Arrays.asList(new Address(to), new Uint256(amount)),
                Collections.singletonList(new TypeReference<Type>() { }));
        return FunctionEncoder.encode(function);
    }
    
}
