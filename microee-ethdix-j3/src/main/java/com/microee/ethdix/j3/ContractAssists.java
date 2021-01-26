package com.microee.ethdix.j3;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.codec.binary.StringUtils;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Bytes32;

public class ContractAssists {

    /**
     * BigInteger 除 BigDecimal
     *
     * @param bigInteger
     * @param decimal
     * @param scale
     * @return
     */
    public static BigDecimal intDivDec(BigInteger bigInteger, BigDecimal decimal, int scale) {
        return toDecimal(bigInteger).divide(decimal, scale, BigDecimal.ROUND_DOWN);
    }

    /**
     * BigDecimal 除 BigInteger
     *
     * @param decimal
     * @param bigInteger
     * @param scale
     * @return
     */
    public static BigDecimal decDivInt(BigDecimal decimal, BigInteger bigInteger, int scale) {
        return decimal.divide(toDecimal(bigInteger), scale, BigDecimal.ROUND_DOWN);
    }

    /**
     * BigDecimal 乘 BigInteger
     *
     * @param decimal
     * @param bigInteger
     * @return
     */
    public static BigDecimal decMulInt(BigDecimal decimal, BigInteger bigInteger) {
        return decimal.multiply(toDecimal(bigInteger));
    }

    /**
     * BigDecimal 减 BigInteger
     *
     * @param decimal
     * @param bigInteger
     * @return
     */
    public static BigDecimal decSubInt(BigDecimal decimal, BigInteger bigInteger) {
        return decimal.subtract(toDecimal(bigInteger));
    }

    public static BigInteger toBigInt(BigDecimal decimal) {
        return new BigInteger(String.valueOf(decimal.setScale(0, BigDecimal.ROUND_DOWN)));
    }

    public static BigDecimal toDecimal(BigInteger bigInteger) {
        return new BigDecimal(String.valueOf(bigInteger));
    }

    public static BigDecimal intDivInt(BigInteger bigInteger1, BigInteger bigInteger2, int scale) {
        return toDecimal(bigInteger1).divide(toDecimal(bigInteger2), scale, BigDecimal.ROUND_DOWN);
    }

    @SuppressWarnings("rawtypes")
    public static String bytes32ToString(String rawData) {
        Function function = new Function("", Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {
                }));
        String value = "";
        try {
            List<Type> types
                    = FunctionReturnDecoder.decode(rawData, function.getOutputParameters());
            value = types.get(0).getValue().toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        if (value.isEmpty()) {
            try {
                Bytes32 bytes32
                        = new Bytes32(org.web3j.utils.Numeric.hexStringToByteArray(rawData));
                value = StringUtils.newStringUsAscii(bytes32.getValue());
            } catch (Exception e) {
                value = "unknown";
            }
        }
        return value.trim();
    }

}
