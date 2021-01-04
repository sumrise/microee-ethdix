package com.microee.ethdix.app.actions;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

import org.assertj.core.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.web3j.tuples.generated.Tuple2;

import com.microee.ethdix.app.components.ETHContractAddressConf;
import com.microee.ethdix.app.components.GasPriceNow;
import com.microee.ethdix.app.components.Web3JFactory;
import com.microee.ethdix.j3.ContractAssists;
import com.microee.ethdix.j3.contract.ERC20ContractQuery;
import com.microee.ethdix.j3.contract.NestOfferPriceContract;
import com.microee.ethdix.j3.contract.RemoteCallFunction;
import com.microee.plugin.response.R;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// 调用预言机查询当前代币价格
@RestController
@RequestMapping("/price")
public class NowPriceRestful {

    private static final Logger LOGGER = LoggerFactory.getLogger(NowPriceRestful.class);

    @Autowired
    private ETHContractAddressConf ethContractAddressConf;

    @Autowired
    private Web3JFactory web3JFactory;

    // 通过 erc20 金额、eth 金额计算代币价格
    // ### 从预言机查询当前代币价格, 以 eth 为单位
    @SuppressWarnings("deprecation")
    @RequestMapping(value = "/nest-pricing", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<BigDecimal> pricing(
            @RequestParam(value = "ethnode", required = false) String ethnode, // 以太坊节点地址
            @RequestParam(value = "network", required = false) String network, // 以太坊网络类型
            @RequestParam(value = "tokenAddress", required = false) String tokenAddress, // 以太坊代币合约地址
            @RequestParam(value = "tokenName", required = false) String tokenName) // 代币名字
            throws Exception {
        Assertions.assertThat((ethnode == null || ethnode.isEmpty()) && (network == null || network.isEmpty())).withFailMessage("ethnode OR network 二选1").isFalse();
        Assertions.assertThat((tokenAddress == null || tokenAddress.isEmpty()) && (tokenName == null || tokenName.isEmpty())).withFailMessage("tokenAddress OR tokenName 二选1").isFalse();
        if (!(tokenName == null || tokenName.trim().isEmpty()) && !(tokenAddress == null || tokenAddress.trim().isEmpty())) {
            // 验证合约名字和合约地址是否匹配
            String newTokenName = ((String) new RemoteCallFunction<>(new ERC20ContractQuery(tokenAddress, web3JFactory.get(network, ethnode)).symbol()).call()).toLowerCase();
            if (!newTokenName.equalsIgnoreCase(tokenName)) {
                return R.failed(R.ILLEGAL, "合约名字和合约地址不匹配");
            }
        }
        if (tokenName == null || tokenName.trim().isEmpty()) {
            tokenName = new ERC20ContractQuery(tokenAddress, web3JFactory.get(network, ethnode)).symbol().send().toLowerCase();
        }
        if (tokenAddress == null || tokenAddress.trim().isEmpty()) {
            tokenAddress = ethContractAddressConf.getContractAddress(network, tokenName);
        }
        NestOfferPriceContract nestOfferPriceContract = new NestOfferPriceContract(ethContractAddressConf.getNestOracleContractAddress(network), web3JFactory.get(network, ethnode)); // 预言机
        Tuple2<BigInteger, BigInteger> latestPrice = nestOfferPriceContract.checkPriceNow(tokenAddress).sendAsync().get();
        BigInteger tokenAmount = latestPrice.getValue1();
        BigInteger erc20Amount = latestPrice.getValue2();
        int tokenDecimalNumber = ((BigInteger) new RemoteCallFunction<>(new ERC20ContractQuery(tokenAddress, web3JFactory.get(network, ethnode)).decimals()).call()).intValue();
        BigDecimal ethDecimal = new BigDecimal("1000000000000000000");
        BigDecimal tokenDecimal = BigDecimal.valueOf(1 * Math.pow(10, tokenDecimalNumber));
        LOGGER.info("currency={}, tokenAddress={}, tokenDecimalNumber={}, tokenDecimal={}", tokenName, tokenAddress, tokenDecimalNumber, tokenDecimal.toPlainString());
        BigDecimal erc20Unit = ContractAssists.intDivDec(erc20Amount, tokenDecimal, 18);
        BigDecimal tokenUnit = ContractAssists.intDivDec(tokenAmount, ethDecimal, 18);
        BigDecimal price = erc20Unit.divide(tokenUnit, 6, RoundingMode.HALF_UP);
        return R.ok(price); // 币种当前价格, 一个 eth 能买 347.23 个 currecny
    }

    // 查询四种 gas 费级别
    @RequestMapping(value = "/now-gasPricing", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<GasPriceNow> gasPricing() {
        GasPriceNow result = GasPriceNow.get();
        if (result != null) {
            result.setRapid((result.getRapid() / Math.pow(10, 9)));
            result.setFast((result.getFast() / Math.pow(10, 9)));
            result.setSlow((result.getSlow() / Math.pow(10, 9)));
            result.setStandard((result.getStandard() / Math.pow(10, 9)));
        }
        return R.ok(result);
    }

    // 根据 gas 费级别计算交易手续费
    @RequestMapping(value = "/eth-transactionFee", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<BigDecimal> transactionFee(
            @RequestParam(value = "gasLevel") String gasLevel,
            @RequestParam(value = "gasLimits", required = false, defaultValue = "21000") Long gasLimits) {
        GasPriceNow result = GasPriceNow.get();
        Double gasPrice = result.gasPrice(gasLevel);
        if (gasPrice == -1.0) {
            return R.failed(R.ILLEGAL, "gasLevel 有误");
        }
        return R.ok(new BigDecimal((gasPrice * gasLimits) / Math.pow(10, 18) + ""));
    }

}
