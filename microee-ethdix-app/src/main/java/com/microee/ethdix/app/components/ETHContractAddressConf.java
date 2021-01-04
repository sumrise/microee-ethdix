package com.microee.ethdix.app.components;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import com.microee.plugin.response.R;
import com.microee.plugin.response.exception.RestException;

// 合约查询浏览器
// blockchair：https://blockchair.com/zh/ethereum/erc-20
@Component
public class ETHContractAddressConf {

    private static final String USDT_CONTRACT_ADDR_FOR_MAINNET = "0xdAC17F958D2ee523a2206206994597C13D831ec7";
    private static final String USDT_CONTRACT_ADDR_FOR_ROPSTEN = null;
    private static final String HBTC_CONTRACT_ADDR_FOR_MAINNET = "0x0316EB71485b0Ab14103307bf65a021042c6d380";
    private static final String HBTC_CONTRACT_ADDR_FOR_ROPSTEN = null;
    private static final String NEST_ORACLE_PRICING_CONTRACT_ADDR_FOR_MAINNET = "0x94F36FAa6bB4f74009637292b09C355CcD3e80Eb";
    private static final String NEST_ORACLE_PRICING_CONTRACT_ADDR_FOR_ROPSTEN = null;
    private static final String UNI_CONTRACT_ADDR_FOR_MAINNET = "0x1f9840a85d5af5bf1d1762f925bdaddc4201f984";
    private static final String UNI_CONTRACT_ADDR_FOR_ROPSTEN = null;
    private static final String UNIV2_CONTRACT_ADDR_FOR_MAINNET = "0xd4405f0704621dbe9d4dea60e128e0c3b26bddbd";
    private static final String UNIV2_CONTRACT_ADDR_FOR_ROPSTEN = null;
    private static final String DAI_CONTRACT_ADDR_FOR_MAINNET = "0x6b175474e89094c44da98b954eedeac495271d0f";
    private static final String DAI_CONTRACT_ADDR_FOR_ROPSTEN = null;

    public String getContractAddress(String network, String currency) {
        if (network.equals("mainnet")) {
            if (currency.equalsIgnoreCase("usdt")) {
                return USDT_CONTRACT_ADDR_FOR_MAINNET;
            }
            if (currency.equalsIgnoreCase("hbtc")) {
                return HBTC_CONTRACT_ADDR_FOR_MAINNET;
            }
            if (currency.equalsIgnoreCase("uni")) {
                return UNI_CONTRACT_ADDR_FOR_MAINNET;
            }
            if (currency.equalsIgnoreCase("UNI-V2")) {
                return UNIV2_CONTRACT_ADDR_FOR_MAINNET;
            }
            if (currency.equalsIgnoreCase("dai")) {
                return DAI_CONTRACT_ADDR_FOR_MAINNET;
            }
        }
        if (network.equals("ropsten")) {
            if (currency.equalsIgnoreCase("usdt")) {
                return USDT_CONTRACT_ADDR_FOR_ROPSTEN;
            }
            if (currency.equalsIgnoreCase("hbtc")) {
                return HBTC_CONTRACT_ADDR_FOR_ROPSTEN;
            }
            if (currency.equalsIgnoreCase("uni")) {
                return UNI_CONTRACT_ADDR_FOR_ROPSTEN;
            }
            if (currency.equalsIgnoreCase("UNI-V2")) {
                return UNIV2_CONTRACT_ADDR_FOR_ROPSTEN;
            }
            if (currency.equalsIgnoreCase("dai")) {
                return DAI_CONTRACT_ADDR_FOR_ROPSTEN;
            }
        }
        throw new RestException(R.FAILED, "不支持的币种或没有配置合约地址");
    }

    public String getNestOracleContractAddress(String network) {
        if (network.equals("mainnet")) {
            return NEST_ORACLE_PRICING_CONTRACT_ADDR_FOR_MAINNET;
        }
        if (network.equals("ropsten")) {
            return NEST_ORACLE_PRICING_CONTRACT_ADDR_FOR_ROPSTEN;
        }
        return null;
    }

    public static final BigDecimal UNIT_ETH = new BigDecimal("1000000000000000000");
    public static final BigDecimal UNIT_USDT = new BigDecimal("1000000");
    public static final BigDecimal UNIT_HBTC = new BigDecimal("1000000000000000000");
    public static final BigDecimal UNIT_UNI = new BigDecimal("1000000000000000000");
    public static final BigDecimal UNIT_UNIV2 = new BigDecimal("1000000000000000000");
    public static final BigDecimal UNIT_DAI = new BigDecimal("1000000000000000000");

    public BigDecimal getUnitDecimals(String symbol) {
        if (symbol.equalsIgnoreCase("eth")) {
            return UNIT_ETH;
        }
        if (symbol.equalsIgnoreCase("USDT")) {
            return UNIT_USDT;
        }
        if (symbol.equalsIgnoreCase("HBTC")) {
            return UNIT_HBTC;
        }
        if (symbol.equalsIgnoreCase("UNI")) {
            return UNIT_UNI;
        }
        if (symbol.equalsIgnoreCase("UNI-V2")) {
            return UNIT_UNIV2;
        }
        if (symbol.equalsIgnoreCase("DAI")) {
            return UNIT_DAI;
        }
        return null;
    }

}
