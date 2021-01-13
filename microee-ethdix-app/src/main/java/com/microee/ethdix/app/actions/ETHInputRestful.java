package com.microee.ethdix.app.actions;

import com.microee.ethdix.app.components.Web3JFactory;
import com.microee.ethdix.j3.contract.ETHInputEncoder;
import com.microee.ethdix.j3.contract.RemoteCallFunction;
import com.microee.ethdix.j3.uniswap.UniswapV2Route02Contract;
import com.microee.ethdix.oem.eth.enums.ChainId;
import java.util.HashMap;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.microee.plugin.response.R;
import java.math.BigInteger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestParam;
import org.web3j.abi.datatypes.Address;

@RestController
@RequestMapping("/input")
public class ETHInputRestful {

    @Autowired
    private Web3JFactory web3JFactory;
    
    // 解析合约里的input参数
    // ===============================================================================
    // 0xa9059cbb000000000000000000000000a17c53f7427979213eaf31def9afaa1d249f764600000000000000000000000000000000000000000000000000000000b7a73adc
    // Function: transfer(address _to, uint256 _value)
    // MethodID: 0xa9059cbb
    // ===============================================================================
    // [0]: 000000000000000000000000a17c53f7427979213eaf31def9afaa1d249f7646
    // [1]: 00000000000000000000000000000000000000000000000000000000b7a73adc
    // ===============================================================================
    //	#	Name	Type		Data
    // ===============================================================================
    //	0	_to	address		0xA17c53F7427979213EAF31DEF9AfAA1D249F7646
    //	1	_value	uint256		3081190108  除以合约的精度就是代币的数量, 假设当前合约是 usdt 合约, 且 usdt 的合约精度是 6 则当前转帐 3000 多usdt
    // ===============================================================================
    @NotNull
    @RequestMapping(value = "/decodeData", method = RequestMethod.POST, consumes = {MediaType.TEXT_PLAIN_VALUE}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<Map<String, Object>> decodeData(@RequestBody String inputData) {
        // String inputData = "0xa9059cbb000000000000000000000000a17c53f7427979213eaf31def9afaa1d249f764600000000000000000000000000000000000000000000000000000000b7a73adc";
        Assertions.assertThat(inputData).withFailMessage("%s 必传", "post body is required").isNotNull().isNotBlank();
        Map<String, Object> map = new HashMap<>();
        if (inputData.equals("0x0")) {
            return R.ok(null);
        }
        if (inputData.startsWith(ETHInputEncoder.TRANSFER_FUNCTION_PREFIX_METHOD_ID)) {
            map.put("Function", "transfer");
            map.put("MethodID", ETHInputEncoder.TRANSFER_FUNCTION_PREFIX_METHOD_ID);
            map.put("To", "0x" + inputData.substring(10, 74).replaceAll("^0+", ""));
            map.put("Value", Long.parseLong(inputData.substring(74).replaceAll("^0+", ""), 16));
        } else if (inputData.startsWith(ETHInputEncoder.APPROVE_FUNCTION_PREFIX_METHOD_ID)) {
            map.put("Function", "approve");
            map.put("MethodID", ETHInputEncoder.APPROVE_FUNCTION_PREFIX_METHOD_ID);
        } else {
            map.put("Function", "N/a");
            map.put("MethodID", "N/a");
        }
        return R.ok(map);
    }
    
    /**
     * 返回 erc20 合约转帐 input 参数
     * @param toAddress
     * @param amount
     * @return 
     */
    @RequestMapping(value = "/getTransferInputData", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<String> getTransferInputData(@RequestParam("toAddress") String toAddress, @RequestParam("amount") Long amount) {
        return R.ok(ETHInputEncoder.getTransferInputData(toAddress, amount));
    }
    
    /**
     * 返回 erc20 合约转帐 input 参数
     * @param toAddress
     * @param amount
     * @return 
     */
    @RequestMapping(value = "/getInputDataForTokenTransfer", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<String> getInputDataForTokenTransfer(@RequestParam("toAddress") String toAddress, @RequestParam("amount") BigInteger amount) {
        return R.ok(ETHInputEncoder.getInputDataForTokenTransfer(toAddress, amount));
    }
    
    /**
     * UniSwap兑换: 获取 eth换代币 input 参数
     * @param network
     * @param amountOutMin 数量
     * @param router02Addr
     * @param tokenAddr 代币地址
     * @param toAddr 用户地址
     * @param timeout 超时时间, 单位/秒
     * @return 
     */
    @RequestMapping(value = "/getInputDataForSwapExactETHForTokens", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<String> getInputDataForSwapExactETHForTokens(
            @RequestParam(value = "chainId", required = false, defaultValue = "mainnet") String chainId, // 网络类型: 主网或测试网
            @RequestParam(value = "router02Addr", required = false, defaultValue = "0x7a250d5630B4cF539739dF2C5dAcb4c659F2488D") String router02Addr,
            @RequestParam("amountOutMin") Long amountOutMin, 
            @RequestParam("tokenAddr") String tokenAddr, 
            @RequestParam("toAddr") String toAddr, 
            @RequestParam("timeout") Long timeout 
    ) {
        Assertions.assertThat(ChainId.get(chainId)).withFailMessage("%s 有误", "chainId").isNotNull();
        Address wethAddr = RemoteCallFunction.build(new UniswapV2Route02Contract(router02Addr, web3JFactory.get(ChainId.get(chainId))).WETH()).call();
        return R.ok(ETHInputEncoder.getInputDataForSwapExactETHForTokens(amountOutMin, wethAddr.getValue(), tokenAddr, toAddr));
    }
    
    
}
