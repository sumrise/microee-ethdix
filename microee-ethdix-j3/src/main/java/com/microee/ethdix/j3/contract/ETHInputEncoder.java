package com.microee.ethdix.j3.contract;

import java.time.Instant;
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
    
    
    // https://app.uniswap.org/#/swap
    // https://uniswap.org/docs/v2/smart-contracts/router02/
    //---------------------------------------------------------------------------------------------------------
    // 0x7ff36ab5
    // 00000000000000000000000000000000000000000000000000000000009d0e0e
    // 0000000000000000000000000000000000000000000000000000000000000080
    // 000000000000000000000000493ba3316c2e246d55edf81427d833631eff9a10
    // 000000000000000000000000000000000000000000000000000000005ff46720
    // 0000000000000000000000000000000000000000000000000000000000000002
    // 000000000000000000000000c02aaa39b223fe8d0a0e5c4f27ead9083c756cc2
    // 000000000000000000000000dac17f958d2ee523a2206206994597c13d831ec7
    
    //Function: swapExactETHForTokens(uint256 amountOutMin, address[] path, address to, uint256 deadline)
    //---------------------------------------------------------------------------------------------------------
    //MethodID: 0x7ff36ab5
    //[0]:  00000000000000000000000000000000000000000000000000000000009d0e0e
    //[1]:  0000000000000000000000000000000000000000000000000000000000000080
    //[2]:  000000000000000000000000493ba3316c2e246d55edf81427d833631eff9a10    // 用户地址
    //[3]:  000000000000000000000000000000000000000000000000000000005ff46720
    //[4]:  0000000000000000000000000000000000000000000000000000000000000002
    //[5]:  000000000000000000000000c02aaa39b223fe8d0a0e5c4f27ead9083c756cc2    // WETH代币合约地址
    //[6]:  000000000000000000000000dac17f958d2ee523a2206206994597c13d831ec7    // USDT代币合约地址
    
    //#	Name            Type        Data
    //----------------------------------------------------------------------------------------------------------
    //0	amountOutMin	uint256     10292750
    //1	path            address[]   0xC02aaA39b223FE8D0A0e5C4F27eAD9083C756Cc2   // WETH代币合约地址
    //                              0xdAC17F958D2ee523a2206206994597C13D831ec7   // USDT代币合约地址
    //2	to              address     0x493bA3316C2E246d55EDf81427D833631EFf9A10   // 用户地址
    //3	deadline	uint256     1609852704
    
    // https://github.com/Uniswap/uniswap-v2-periphery/blob/master/contracts/UniswapV2Router02.sol
    // function swapExactETHForTokens(uint amountOutMin, address[] calldata path, address to, uint deadline)
    // external
    // payable
    // returns (uint[] memory amounts);
    // 0xa9059cbb0000000000000000000000009e7b1a22cf4d69efad71bac24a6a8518574f1fc40000000000000000000000000000000000000000000000000000008fe0b0b380
    // UniSwap兑换: 获取 eth换代币 input 参数
    public static String getInputDataForSwapExactETHForTokens(Long amountOutMin, String wethAddr, String tokenAddr, String toAddr, Long timeout) {
        Long deadline = Instant.now().toEpochMilli() + (timeout * 1000);
        Function function = new Function(
                "swapExactETHForTokens",
                Arrays.asList(new Uint256(amountOutMin), new Address(wethAddr), new Address(tokenAddr), new Address(toAddr), new Uint256(deadline)),
                Collections.singletonList(new TypeReference<Type>() { }));
        return FunctionEncoder.encode(function).replace("0xa399e043", "0x7ff36ab5");
    }
    
}
