package com.microee.ethdix.j3.contract;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tuples.generated.Tuple2;
import org.web3j.tx.Contract;
import org.web3j.tx.ReadonlyTransactionManager;
import org.web3j.tx.TransactionManager;
import com.microee.ethdix.j3.Constrants;
import com.microee.ethdix.j3.contract.ERC20ContractQuery.TypeOf;

// https://github.com/NEST-Protocol/NEST-oracle-V3
// checkPriceNow
// https://github.com/NEST-Protocol/NEST-oracle-V3/blob/master/NestOffer/Nest_3_OfferPrice.sol
// NEST 价格合约地址
public class NestOfferPriceContract extends Contract {
    
    private static final String BINARY = "";

    @SuppressWarnings("deprecation")
    public NestOfferPriceContract(String contractAddress, Web3j web3j) {
        super(BINARY, contractAddress, web3j, new ReadonlyTransactionManager(web3j, Constrants.EMPTY_ADDRESS),  Contract.GAS_PRICE, Contract.GAS_LIMIT);
    }
    
    @SuppressWarnings("deprecation")
    protected NestOfferPriceContract(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    @SuppressWarnings("deprecation")
    protected NestOfferPriceContract(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

//    public List<NowTokenPriceEventResponse> getNowTokenPriceEvents(TransactionReceipt transactionReceipt) {
//        final Event event = new Event("NowTokenPrice",
//                Arrays.<TypeReference<?>>asList(),
//                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}));
//        List<EventValuesWithLog> valueList = extractEventParametersWithLog(event, transactionReceipt);
//        ArrayList<NowTokenPriceEventResponse> responses = new ArrayList<NowTokenPriceEventResponse>(valueList.size());
//        for (EventValuesWithLog eventValues : valueList) {
//            NowTokenPriceEventResponse typedResponse = new NowTokenPriceEventResponse();
//            typedResponse.log = eventValues.getLog();
//            typedResponse.a = (String) eventValues.getNonIndexedValues().get(0).getValue();
//            typedResponse.b = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
//            typedResponse.c = (BigInteger) eventValues.getNonIndexedValues().get(2).getValue();
//            responses.add(typedResponse);
//        }
//        return responses;
//    }
//
//    public Observable<NowTokenPriceEventResponse> nowTokenPriceEventObservable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
//        final Event event = new Event("NowTokenPrice",
//                Arrays.<TypeReference<?>>asList(),
//                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}));
//        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
//        filter.addSingleTopic(EventEncoder.encode(event));
//        return web3j.ethLogObservable(filter).map(new Func1<Log, NowTokenPriceEventResponse>() {
//            @Override
//            public NowTokenPriceEventResponse call(Log log) {
//                EventValuesWithLog eventValues = extractEventParametersWithLog(event, log);
//                NowTokenPriceEventResponse typedResponse = new NowTokenPriceEventResponse();
//                typedResponse.log = log;
//                typedResponse.a = (String) eventValues.getNonIndexedValues().get(0).getValue();
//                typedResponse.b = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
//                typedResponse.c = (BigInteger) eventValues.getNonIndexedValues().get(2).getValue();
//                return typedResponse;
//            }
//        });
//    }

    @SuppressWarnings("rawtypes")
    public RemoteCall<TransactionReceipt> updateAndCheckPriceList(String tokenAddress, BigInteger num, BigInteger weiValue) {
        final Function function = new Function(
                "updateAndCheckPriceList",
                Arrays.<Type>asList(new Address(tokenAddress),
                new Uint256(num)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function, weiValue);
    }

    @SuppressWarnings("rawtypes")
    public RemoteCall<TransactionReceipt> addPrice(BigInteger ethAmount, BigInteger tokenAmount, BigInteger endBlock, String tokenAddress, String offerOwner) {
        final Function function = new Function(
                "addPrice",
                Arrays.<Type>asList(new Uint256(ethAmount),
                new Uint256(tokenAmount),
                new Uint256(endBlock),
                new Address(tokenAddress),
                new Address(offerOwner)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    @SuppressWarnings("rawtypes")
    public RemoteCall<Tuple2<BigInteger, BigInteger>> checkPriceCostProportion() {
        final Function function = new Function("checkPriceCostProportion",
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeOf<Uint256>().get(), new TypeOf<Uint256>().get()));
        return new RemoteCall<Tuple2<BigInteger, BigInteger>>(
                new Callable<Tuple2<BigInteger, BigInteger>>() {
                    @Override
                    public Tuple2<BigInteger, BigInteger> call() throws Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);
                        return new Tuple2<BigInteger, BigInteger>(
                                (BigInteger) results.get(0).getValue(),
                                (BigInteger) results.get(1).getValue());
                    }
                });
    }

    @SuppressWarnings("rawtypes")
    public RemoteCall<Tuple2<BigInteger, BigInteger>> checkPriceNow(String tokenAddress) {
        final Function function = new Function("checkPriceNow",
                Arrays.<Type>asList(new Address(tokenAddress)),
                Arrays.<TypeReference<?>>asList(new TypeOf<Uint256>().get(), new TypeOf<Uint256>().get()));
        return new RemoteCall<Tuple2<BigInteger, BigInteger>>(
                new Callable<Tuple2<BigInteger, BigInteger>>() {
                    @Override
                    public Tuple2<BigInteger, BigInteger> call() throws Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);
                        return new Tuple2<BigInteger, BigInteger>(
                                (BigInteger) results.get(0).getValue(),
                                (BigInteger) results.get(1).getValue());
                    }
                });
    }

    @SuppressWarnings("rawtypes")
    public RemoteCall<BigInteger> checkPriceCost() {
        final Function function = new Function("checkPriceCost",
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeOf<Uint256>().get()));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    @SuppressWarnings("rawtypes")
    public RemoteCall<TransactionReceipt> changePrice(BigInteger ethAmount, BigInteger tokenAmount, String tokenAddress, BigInteger blockNum) {
        final Function function = new Function(
                "changePrice",
                Arrays.<Type>asList(new Uint256(ethAmount),
                new Uint256(tokenAmount),
                new Address(tokenAddress),
                new Uint256(blockNum)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    @SuppressWarnings("rawtypes")
    public RemoteCall<TransactionReceipt> changePriceCostProportion(BigInteger user, BigInteger abonus) {
        final Function function = new Function(
                "changePriceCostProportion",
                Arrays.<Type>asList(new Uint256(user),
                new Uint256(abonus)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    @SuppressWarnings("rawtypes")
    public RemoteCall<Tuple2<BigInteger, BigInteger>> checkPriceForBlock(String tokenAddress, BigInteger blockNum) {
        final Function function = new Function("checkPriceForBlock",
                Arrays.<Type>asList(new Address(tokenAddress),
                new Uint256(blockNum)),
                Arrays.<TypeReference<?>>asList(new TypeOf<Uint256>().get(), new TypeOf<Uint256>().get()));
        return new RemoteCall<Tuple2<BigInteger, BigInteger>>(
                new Callable<Tuple2<BigInteger, BigInteger>>() {
                    @Override
                    public Tuple2<BigInteger, BigInteger> call() throws Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);
                        return new Tuple2<BigInteger, BigInteger>(
                                (BigInteger) results.get(0).getValue(),
                                (BigInteger) results.get(1).getValue());
                    }
                });
    }

    @SuppressWarnings("rawtypes")
    public RemoteCall<TransactionReceipt> changePriceCost(BigInteger amount) {
        final Function function = new Function(
                "changePriceCost",
                Arrays.<Type>asList(new Uint256(amount)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    @SuppressWarnings("rawtypes")
    public RemoteCall<TransactionReceipt> changeMapping(String voteFactory) {
        final Function function = new Function(
                "changeMapping",
                Arrays.<Type>asList(new Address(voteFactory)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    @SuppressWarnings("rawtypes")
    public RemoteCall<TransactionReceipt> updateAndCheckPriceNow(String tokenAddress, BigInteger weiValue) {
        final Function function = new Function(
                "updateAndCheckPriceNow",
                Arrays.<Type>asList(new Address(tokenAddress)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function, weiValue);
    }

    @SuppressWarnings("rawtypes")
    public RemoteCall<TransactionReceipt> changeBlackList(String add, Boolean isBlack) {
        final Function function = new Function(
                "changeBlackList",
                Arrays.<Type>asList(new Address(add),
                new Bool(isBlack)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    @SuppressWarnings("rawtypes")
    public RemoteCall<Boolean> checkBlackList(String add) {
        final Function function = new Function("checkBlackList",
                Arrays.<Type>asList(new Address(add)),
                Arrays.<TypeReference<?>>asList(new TypeOf<Bool>().get()));
        return executeRemoteCallSingleValueReturn(function, Boolean.class);
    }

    @SuppressWarnings("rawtypes")
    public static RemoteCall<NestOfferPriceContract> deploy(Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit, String voteFactory) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new Address(voteFactory)));
        return deployRemoteCall(NestOfferPriceContract.class, web3j, credentials, gasPrice, gasLimit, BINARY, encodedConstructor);
    }

    @SuppressWarnings("rawtypes")
    public static RemoteCall<NestOfferPriceContract> deploy(Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit, String voteFactory) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new Address(voteFactory)));
        return deployRemoteCall(NestOfferPriceContract.class, web3j, transactionManager, gasPrice, gasLimit, BINARY, encodedConstructor);
    }

    public static NestOfferPriceContract load(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return new NestOfferPriceContract(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    public static NestOfferPriceContract load(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new NestOfferPriceContract(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

//    public static class NowTokenPriceEventResponse {
//        public Log log;
//        public String a;
//        public BigInteger b;
//        public BigInteger c;
//    }

}
