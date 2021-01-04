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
import com.microee.ethdix.j3.Assists;
import com.microee.ethdix.j3.contract.ERC20ContractQuery;
import com.microee.ethdix.j3.contract.NestOfferPriceContract;
import com.microee.plugin.response.R;

// 调用预言机查询当前代币价格
@RestController
@RequestMapping("/price")
public class NowPriceRestful {

    // private static final String PRICE_CACHE_KEY = "__gas_price_now";
    // private static final int PRICE_CACHE_EXPIRED_SEC = 5; // 过期时间/秒
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
            @RequestParam(value = "network", required = false) String network, // 以太坊节点地址
            @RequestParam(value = "tokenAddress", required = false) String tokenContractAddress, // 以太坊节点地址
            @RequestParam(value = "tokenName", required = false) String tokenName) // 代币名字
            throws Exception {
        Assertions.assertThat((ethnode == null || ethnode.isEmpty()) && (network == null || network.isEmpty())).withFailMessage("ethnode OR network 二选1").isFalse();
        Assertions.assertThat((tokenContractAddress == null || tokenContractAddress.isEmpty()) && (tokenName == null || tokenName.isEmpty())).withFailMessage("tokenContractAddress OR tokenName 二选1").isFalse();
        if (!(tokenName == null || tokenName.trim().isEmpty()) && !(tokenContractAddress == null || tokenContractAddress.trim().isEmpty())) {
            String newTokenName = new ERC20ContractQuery(tokenContractAddress, web3JFactory.get(network, ethnode)).symbol().send().toLowerCase();
            if (!newTokenName.equalsIgnoreCase(tokenName)) {
                return R.failed(R.ILLEGAL, "合约名字和合约地址不匹配");
            }
        } else {
            if (tokenName == null || tokenName.trim().isEmpty()) {
                tokenName = new ERC20ContractQuery(tokenContractAddress, web3JFactory.get(network, ethnode)).symbol().send().toLowerCase();
            }
            if (tokenContractAddress == null || tokenContractAddress.trim().isEmpty()) {
                tokenContractAddress = ethContractAddressConf.getContractAddress(network, tokenName);
            }
        }
        NestOfferPriceContract nestOfferPriceContract = new NestOfferPriceContract(ethContractAddressConf.getNestOracleContractAddress(network), web3JFactory.get(network, ethnode)); // 预言机
        Tuple2<BigInteger, BigInteger> latestPrice = nestOfferPriceContract.checkPriceNow(tokenContractAddress).sendAsync().get();
        BigInteger tokenAmount = latestPrice.getValue1();
        BigInteger erc20Amount = latestPrice.getValue2();
        BigDecimal erc20Unit = Assists.intDivDec(erc20Amount, ethContractAddressConf.getUnitDecimals(tokenName), 18);
        BigDecimal tokenUnit = Assists.intDivDec(tokenAmount, ethContractAddressConf.getUnitDecimals("eth"), 18);
        BigDecimal price = erc20Unit.divide(tokenUnit, 48, RoundingMode.HALF_UP);
        return R.ok(price); // 币种当前价格, 一个 eth 能买 347.23 个 currecny
    }

    // 查询四种 gas 费级别
    @RequestMapping(value = "/now-gasPricing", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<GasPriceNow> gasPricing() {
        GasPriceNow result = GasPriceNow.get();
        if (result != null) {
            result.setRapid((long) (result.getRapid() / Math.pow(10, 9)));
            result.setFast((long) (result.getFast() / Math.pow(10, 9)));
            result.setSlow((long) (result.getSlow() / Math.pow(10, 9)));
            result.setStandard((long) (result.getStandard() / Math.pow(10, 9)));
        }
        return R.ok(result);
    }

    // 根据 gas 费级别计算交易手续费
    @RequestMapping(value = "/eth-transactionFee", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<BigDecimal> transactionFee(@RequestParam(value = "gasLevel") String gasLevel) {
        GasPriceNow result = GasPriceNow.get();
        if (result != null) {
            Long gasPrice = result.gasPrice(gasLevel);
            if (gasPrice == -1l) {
                return R.failed(R.ILLEGAL, "gasLevel 有误");
            }
            return R.ok(new BigDecimal((gasPrice * 21000l) / Math.pow(10, 18) + ""));
        }
        return R.ok(null);
    }

}
