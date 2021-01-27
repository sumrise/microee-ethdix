package com.microee.ethdix.j3.rpc;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Hash;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionDecoder;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.utils.Numeric;
import com.fasterxml.jackson.core.type.TypeReference;
import com.microee.ethdix.j3.wss.ETHMessageListener;
import com.microee.ethdix.j3.wss.ETHWebSocketFactory;
import com.microee.ethdix.oem.eth.EthRawBlock;
import com.microee.ethdix.oem.eth.EthRawTransaction;
import com.microee.ethdix.oem.eth.EthTransactionReceipt;
import com.microee.plugin.commons.Helper;
import com.microee.plugin.commons.Helper.KV;
import com.microee.plugin.http.assets.HttpAssets;
import com.microee.plugin.http.assets.HttpClient;
import com.microee.plugin.http.assets.HttpClientResult;
import com.microee.plugin.response.R;
import com.microee.plugin.response.exception.RestException;
import okhttp3.Headers;

/**
 * 接口文档 https://ethereum.org/zh/what-is-ethereum/ https://infura.io/docs
 * https://eth.wiki/json-rpc/API
 */
public class JsonRPC {

    private static final Logger logger = LoggerFactory.getLogger(JsonRPC.class);

    private final HttpClient httpClient;
    private final String[] ethnodes;
    private String wss;
    private ETHWebSocketFactory webSocketFactoryMainnet;
    private Headers authHeaders;

    public JsonRPC(String ethnode) {
        this.ethnodes = new String[] {ethnode};
        this.httpClient = HttpClient.create();
    }

    public JsonRPC(String ethnode, String httpUsername, String httpPasswd) {
        this.ethnodes = new String[] {ethnode};
        this.httpClient = HttpClient.create();
        this.authHeaders = Headers.of("Authorization", okhttp3.Credentials.basic(httpUsername, httpPasswd));
    }

    public JsonRPC(String[] ethnodes) {
        this.ethnodes = ethnodes == null ? null : ethnodes.clone();
        this.httpClient = HttpClient.create();
    }

    public JsonRPC(List<String> ethnodes, String wss, ETHMessageListener ethMessageListener) {
        this.ethnodes = ethnodes.toArray(new String[ethnodes.size()]);
        this.wss = wss;
        this.httpClient = HttpClient.create();
        this.webSocketFactoryMainnet = ETHWebSocketFactory.build(wss, ethMessageListener);
    }

    // 连接 WebSocket
    public JsonRPC connect() {
        this.webSocketFactoryMainnet.connect();
        return this;
    }

    // ws 订阅
    public Boolean subscribe(String string) {
        this.webSocketFactoryMainnet.subscribe(string);
        return true;
    }

    public String[] getEthNodeList() {
        return this.ethnodes.clone();
    }

    public String getWss() {
        return wss;
    }

    public void setWss(String wss) {
        this.wss = wss;
    }

    /**
     * 返回链id
     * 
     * @return
     */
    public Long getChainId() {
        String methodName = "eth_chainId";
        String chainId =
                this.post(methodName, new Object[] {}, new TypeOf<JsonRpcResponse<String>>().get()).getResult();
        return Long.parseLong(chainId.substring(2));
    }

    /**
     * 返回当前账户的所有地址 Infura 不支持解锁账户, 所以该接口不会返回任何账户地址, 当使用 Infura 接口时需发送加密后的数据
     *
     * @return
     */
    public String[] accounts() {
        String methodName = "eth_accounts";
        Object params = new Object[] {};
        return this.post(methodName, params, new TypeOf<JsonRpcResponse<String[]>>().get()).getResult();
    }

    /**
     * 查询合约代码
     *
     * @param address
     * @return
     */

    public String getCode(String address) {
        String methodName = "eth_getCode";
        Object params = new Object[] {address, "latest"};
        return this.post(methodName, params, new TypeOf<JsonRpcResponse<String>>().get()).getResult();
    }

    //@formatter:off
    // 交易费用（gas）
    // ======================================================================================
    // 以太坊的运行环境，也被称为以太坊虚拟机（EVM）。每个参与到网络的节点都会运行EVM作为区块验证协议的一部分。每个网络中的全节点都会进行相同的计算并储存相同的值。
    // 合约执行会在所有节点中被多次重复，而且任何人都可以发布执行合约，这使得合约执行的消耗非常昂贵，所以为防止以太坊网络发生蓄意攻击或滥用的现象，以太坊协议规定交易或合约调用的每个运算步骤都需要收费。
    // 这笔费用以gas作为单位计数，也就是俗称的燃料。
    // 根据以太坊协议，在合约或交易中执行的每个计算步骤都要收取费用，以防止在以太坊网络上的恶意攻击和滥用。
    // 每笔交易都必须包含 gas limit 和愿意为 gas 支付的费用。矿工可以选择是否打包交易和收取费用。
    // 如果由交易产生的计算步骤所使用的 gas 总量(gas used )，包括原始消息和可能被触发的任何子消息，小于或等于gas limit，则处理该交易。
    // 如果 gas 总量超过 gas limit，那么所有的改变都会回退，除非交易仍然有效并且矿工接受了这个费用。交易执行中未使用的所有多余的 gas 将以 Ether 返还给交易发起人。
    // 交易中花费的总共的 ether 成本取决于2个因素：
    // 1）gasUsed: 是交易中消耗的总共的 gas
    // 2）gasPrice：在交易中指定一个单位 gas 的价格（ether）总费用 = gasUsed * gasPrice EVM 中的每个操作都指定了要消耗的 gas 量。 gasUsed 是执行所有操作的所有 gas 的总和。
    // gasUsed 有三种不同构成：
    // 1）计算操作的固定费用
    // 2）交易（合约创建或消息调用）费用
    // 3）存储（内存、存储账户合约数据）费用
    // 其中，存储收费是因为假如你的合约使得状态数据库存储增大，所有节点都会增加存储。所以，以太币是鼓励尽量保持少量存储的。但是如果有操作是清除一个存储条目，这个操作的费用不但会被免除，而且由于释放空间还会获得退款。
    //@formatter:on
    //@formatter:off
    // 交易的随机数（nonce）
    // ======================================================================================
    // 每个以太坊账户都有一个叫做 nonce 的字段，来记录该账户已执行的交易总数。
    // 为了防止交易重播，ETH（ETC）节点要求每笔交易必须有一个 nonce 数值。每一个账户从同一个节点发起交易时，这个 nonce 值从 0 开始计数，发送一笔 nonce 对应加 1。
    // 当前面的 nonce 处理完成之后才会处理后面的 nonce。前提条件是相同的地址在相同的节点发送交易。 nonce 值也用于防止帐户余额的错误计算。在以太坊这样的分布式系统中，节点可能无序地接收交易。nonce 强制任何地址的交易按顺序处理，不管间隔时间如何，无论节点接收到的顺序如何。
    // 以下是 nonce 使用的几条规则：
    // 当 nonce 太小（小于之前已经有交易使用的 nonce 值），交易会被拒绝。
    // 当 nonce 太大，交易会一直处于队列之中；
    // 当发送一个比较大的 nonce 值，然后补齐开始 nonce 到那个值之间的 nonce，那么交易依旧可以被执行。
    // 当交易处于 queue 中时停止 geth 客户端，那么交易 queue 中的交易会被清除掉。
    //@formatter:on
    //@formatter:off
    // RLP编码
    // ======================================================================================
    // RLP(Recursive Length Prefix，递归长度前缀)是一种序列化编码算法，用于编码任意的嵌套结构的二进制数据。
    // RLP序列化方法因为简单、短小等诸多优点，现如今已经成为以太坊中数据序列化/反序列化的主要方法，区块、交易等数据结构在持久化时会先经过RLP编码后再存储到数据库中。
    //@formatter:on
    /**
     * 创建 eth 转帐签名
     *
     * @param fromAddress
     * @param to
     * @param gasPrice
     * @param gasLimit
     * @param amount
     * @param privateKey
     * @return 返回 signedTransactionData
     */
    public String signETHTransaction(String fromAddress, String to, Long gasPrice, Long gasLimit,
            BigInteger amount, String privateKey) {
        BigInteger privateKeyInBT = new BigInteger(privateKey, 16);
        ECKeyPair aPair = ECKeyPair.create(privateKeyInBT);
        Long nonce = this.getTransactionCount(fromAddress);
        RawTransaction rawTransaction = RawTransaction.createEtherTransaction(BigInteger.valueOf(nonce), BigInteger.valueOf(gasPrice), BigInteger.valueOf(gasLimit), to, amount);
        // byte[] encodedMessage = TransactionEncoder.encode(rawTransaction);
        // RawTransaction result = TransactionDecoder.decode(Numeric.toHexString(encodedMessage));
        // logger.info("nonce={}", result.getNonce());
        // logger.info("gasPrice={}", result.getGasPrice());
        // logger.info("gasLimit={}", result.getGasLimit());
        // logger.info("to={}", result.getTo());
        // logger.info("value={}", result.getValue());
        // logger.info("data={}", result.getData());
        return Numeric.toHexString(TransactionEncoder.signMessage(rawTransaction, Credentials.create(aPair)));
    }

    /**
     * erc20 转帐签名
     * 
     * @param fromAddress
     * @param to
     * @param gasPrice
     * @param gasLimit
     * @param privateKey
     * @param hexData
     * @return
     */
    public String signTokenTransaction(String fromAddress, String to, Long gasPrice, Long gasLimit,
            String privateKey, String hexData) {
        BigInteger privateKeyInBT = new BigInteger(privateKey, 16);
        ECKeyPair aPair = ECKeyPair.create(privateKeyInBT);
        Long nonce = this.getTransactionCount(fromAddress);
        RawTransaction rawTransaction = RawTransaction.createTransaction(BigInteger.valueOf(nonce),
                BigInteger.valueOf(gasPrice), BigInteger.valueOf(gasLimit), to,
                BigInteger.valueOf(0), hexData);
        byte[] encodedMessage = TransactionEncoder.encode(rawTransaction);
        RawTransaction result = TransactionDecoder.decode(Numeric.toHexString(encodedMessage));
        logger.info("nonce={}", result.getNonce());
        logger.info("gasPrice={}", result.getGasPrice());
        logger.info("gasLimit={}", result.getGasLimit());
        logger.info("to={}", result.getTo());
        logger.info("value={}", result.getValue());
        logger.info("data={}", result.getData());
        return Numeric.toHexString(TransactionEncoder.signMessage(rawTransaction, Credentials.create(aPair)));
    }

    /**
     * 解码交易数据
     * 
     * @param signedTransactionData 签名后的交易数据
     * @return
     */
    public static RawTransaction decodeTransactionHash(String signedTransactionData) {
        return TransactionDecoder.decode(signedTransactionData);
    }

    /**
     * 预估 gas limits
     * 
     * @param from
     * @param to
     * @param gasPrice
     * @param value
     * @return
     */
    public Long estimateGasLimits(String from, String to, Long gasPrice, Long value) {
        String methodName = "eth_estimateGas";
        Object params = new Object[] {Helper.mapOf(KV.of("from", from), KV.of("to", to),
                KV.of("gasPrice", "0x" + Long.toHexString(gasPrice)),
                KV.of("value", value == null ? null : "0x" + Long.toHexString(value)))};
        String balance = this.post(methodName, params, new TypeOf<JsonRpcResponse<String>>().get()).getResult();
        return Long.parseLong(balance.substring(2), 16);
    }

    /**
     * 预估 gas limits
     * 
     * @param from
     * @param to
     * @param gasPrice
     * @param data
     * @return
     */
    public Long estimateGasLimits(String from, String to, Long gasPrice, String data) {
        String methodName = "eth_estimateGas";
        Object params = new Object[] {Helper.mapOf(KV.of("from", from), KV.of("to", to),
                KV.of("gasPrice", "0x" + Long.toHexString(gasPrice)), KV.of("value", "0x0"),
                KV.of("data", data))};
        String balance = this.post(methodName, params, new TypeOf<JsonRpcResponse<String>>().get()).getResult();
        return Long.parseLong(balance.substring(2), 16);
    }

    /**
     * 根据交易数据获取交易哈希
     * 
     * @param signedTransactionData
     * @return
     */
    public static String getTransactionHash(String signedTransactionData) {
        return Hash.sha3(signedTransactionData);
    }

    /**
     * 解码交易数据
     * 
     * @param signedTransactionData
     * @return
     */
    public static RawTransaction decodeSignedTransactionData(String signedTransactionData) {
        return TransactionDecoder.decode(signedTransactionData);
    }

    /**
     * 发送交易
     *
     * @param signedTransactionData
     * @return
     */
    public String sendRawTransaction(String signedTransactionData) {
        String methodName = "eth_sendRawTransaction";
        Object params = new Object[] {signedTransactionData};
        try {
            return this.post(methodName, params, new TypeOf<JsonRpcResponse<String>>().get()).getResult();
        } catch (Exception e) {
            logger.error("发送交易异常: signedTransactionData={}, errorMessage={}", signedTransactionData, e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取交易基本信息
     *
     * @param hash 交易哈希, 例如: 0xd0567b3ec6e740d40ad1c0ad1e1143fea56c3d9edcfd7529e74a2aa6d8826544
     * @return
     */
    public EthRawTransaction getTransactionByHash(String hash) {
        String methodName = "eth_getTransactionByHash";
        Object params = new Object[] {hash};
        return this.post(methodName, params, new TypeOf<JsonRpcResponse<EthRawTransaction>>().get()).getResult();
    }

    /**
     * 查询当前地址在当前节点上的交易数量
     *
     * @param accountAddress 以太坊账户地址
     * @return
     */
    public Long getTransactionCount(String accountAddress) {
        String methodName = "eth_getTransactionCount";
        Object params = new Object[] {accountAddress, "latest"};
        String transactionCountString = this.post(methodName, params, new TypeOf<JsonRpcResponse<String>>().get()).getResult();
        if (transactionCountString.length() > 2) {
            return Long.parseLong(transactionCountString.substring(2), 16);
        }
        return null;
    }

    /**
     * 根据交易哈希获取交易回执
     *
     * @param hash 交易哈希, 例如: 0xd0567b3ec6e740d40ad1c0ad1e1143fea56c3d9edcfd7529e74a2aa6d8826544
     * @return
     */
    public EthTransactionReceipt getTransactionReceipt(String hash) {
        String methodName = "eth_getTransactionReceipt";
        Object params = new Object[] {hash};
        return this.post(methodName, params, new TypeOf<JsonRpcResponse<EthTransactionReceipt>>().get()).getResult();
    }

    // 获取当前块高
    public long blockNumber() {
        String methodName = "eth_blockNumber";
        Object params = new Object[] {};
        String result = this.post(methodName, params, new TypeOf<JsonRpcResponse<String>>().get()).getResult();
        if (result == null || result.isEmpty()) {
            return 0l;
        }
        return Long.parseLong(result.replace("0x", ""), 16);
    }

    // 根据高度获取区块
    // {"jsonrpc":"2.0","id":"56ffc283-d604-4d4f-acf2-0e11bfaca5a6","result":{"difficulty":"0x3ff800000","extraData":"0x476574682f76312e302e302f6c696e75782f676f312e342e32","gasLimit":"0x1388","gasUsed":"0x0","hash":"0x88e96d4537bea4d9c05d12549907b32561d3bf31f45aae734cdc119f13406cb6","logsBloom":"0x00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000","miner":"0x05a56e2d52c817161883f50c441c3228cfe54d9f","mixHash":"0x969b900de27b6ac6a67742365dd65f55a0526c41fd18e1b16f1a1215c2e66f59","nonce":"0x539bd4979fef1ec4","number":"0x1","parentHash":"0xd4e56740f876aef8c010b86a40d5f56745a118d0906a34e69aec8c0db1cb8fa3","receiptsRoot":"0x56e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421","sha3Uncles":"0x1dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347","size":"0x219","stateRoot":"0xd67e4d450343046425ae4271474353857ab860dbc0a1dde64b41b5cd3a532bf3","timestamp":"0x55ba4224","totalDifficulty":"0x7ff800000","transactions":[],"transactionsRoot":"0x56e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421","uncles":[]}}
    public EthRawBlock getBlockByNumber(long height) {
        return this.getBlockByHexNumber("0x" + Long.toHexString(height));
    }

    // 查询平台币余额
    public Long getQueryEthBalance(String accountAddress) {
        String methodName = "eth_getBalance";
        Object params = new Object[] {accountAddress, "latest"};
        String balance = this.post(methodName, params, new TypeOf<JsonRpcResponse<String>>().get()).getResult();
        return Long.parseLong(balance.substring(2), 16);
    }

    // 根据高度获取区块
    public EthRawBlock getBlockByHexNumber(String hexLong) {
        String methodName = "eth_getBlockByNumber";
        Object params = new Object[] {hexLong, true};
        return this.post(methodName, params, new TypeOf<JsonRpcResponse<EthRawBlock>>().get()).getResult();
    }

    // 根据区块哈希取得区块
    public EthRawBlock getBlockByHash(String bockHash) {
        String methodName = "eth_getBlockByHash";
        Object params = new Object[] {bockHash, true};
        return this.post(methodName, params, new TypeOf<JsonRpcResponse<EthRawBlock>>().get()).getResult();
    }

    public <T> T post(String method, Object params, TypeReference<T> typeRef) {
        HttpClientResult httpResult = this.httpClient.postJsonBody(UsedCount.getEthNode(this.ethnodes), authHeaders, JsonRpcRequest.json(method, params));
        if (httpResult == null) {
            throw new RestException(R.TIME_OUT, "查询超时");
        }
        if (!httpResult.isSuccess()) {
            throw new RestException(R.FAILED, "查询失败");
        }
        return HttpAssets.parseJson(httpResult.getResult(), typeRef);
    }

    public String getEthNode() {
        return UsedCount.getEthNode(this.ethnodes);
    }

    public void inputDecoder() {

    }

    public static class UsedCount {

        public static final ConcurrentHashMap<String, ConcurrentHashMap<String, AtomicInteger>> usedCount =
                new ConcurrentHashMap<>();

        public static int incr(String ethNode) {
            String currentDate = currentDateString();
            if (!usedCount.containsKey(currentDate)) {
                usedCount.putIfAbsent(currentDate, new ConcurrentHashMap<>());
            }
            ConcurrentHashMap<String, AtomicInteger> map = usedCount.get(currentDate);
            if (!map.containsKey(ethNode)) {
                map.putIfAbsent(ethNode, new AtomicInteger(0));
            }
            return map.get(ethNode).addAndGet(1);
        }

        public static String getEthNode(String[] ethNodes) {
            String currentDate = currentDateString();
            if (!UsedCount.get().containsKey(currentDate)) {
                UsedCount.incr(ethNodes[0]);
                return ethNodes[0];
            }
            Map<String, Integer> map = UsedCount.get().get(currentDate);
            for (String node : ethNodes) {
                if (!map.containsKey(node)) {
                    UsedCount.incr(node);
                    return node;
                }
            }
            String currentNode = sortByValue(map).entrySet().iterator().next().getKey();
            UsedCount.incr(currentNode);
            return currentNode;
        }

        // 根据 value 排序 map
        public static Map<String, Integer> sortByValue(Map<String, Integer> unsortMap) {
            List<Entry<String, Integer>> list = new LinkedList<>(unsortMap.entrySet());
            // Sorting the list based on values
            list.sort((o1, o2) -> o1.getValue().compareTo(o2.getValue()));
            return list.stream().collect(Collectors.toMap(Entry::getKey, Entry::getValue,
                    (a, b) -> b, LinkedHashMap::new));

        }

        public static Map<String, Map<String, Integer>> get() {
            Map<String, Map<String, Integer>> result = new HashMap<>();
            for (Entry<String, ConcurrentHashMap<String, AtomicInteger>> entry : UsedCount.usedCount.entrySet()) {
                Map<String, Integer> map = new HashMap<>();
                for (Entry<String, AtomicInteger> e : entry.getValue().entrySet()) {
                    map.put(e.getKey(), e.getValue().intValue());
                }
                result.put(entry.getKey(), map);
            }
            return result;
        }

        public static String currentDateString() {
            return new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        }

    }

    public static class JsonRpcRequest {

        private String id;
        private String jsonrpc = "2.0";
        private String method;
        private Object params;

        public static String json(String method, Object params) {
            return new JsonRpcRequest(method, params).toString();
        }

        public JsonRpcRequest(String method, Object params) {
            this.id = UUID.randomUUID().toString();
            this.method = method;
            this.params = params;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getJsonrpc() {
            return jsonrpc;
        }

        public void setJsonrpc(String jsonrpc) {
            this.jsonrpc = jsonrpc;
        }

        public String getMethod() {
            return method;
        }

        public void setMethod(String method) {
            this.method = method;
        }

        public Object getParams() {
            return params;
        }

        public void setParams(Object params) {
            this.params = params;
        }

        @Override
        public String toString() {
            return HttpAssets.toJsonString(this);
        }

    }

    public static class JsonRpcResponse<T> {

        private String id;
        private String jsonrpc;
        private Error error;
        private String desc;
        private T result;

        public JsonRpcResponse() {

        }

        public JsonRpcResponse(String id, String jsonrpc, Error error, String desc, T result) {
            super();
            this.id = id;
            this.jsonrpc = jsonrpc;
            this.error = error;
            this.desc = desc;
            this.result = result;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getJsonrpc() {
            return jsonrpc;
        }

        public void setJsonrpc(String jsonrpc) {
            this.jsonrpc = jsonrpc;
        }

        public Error getError() {
            return error;
        }

        public void setError(Error error) {
            this.error = error;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }

        public T getResult() {
            if (this.getError() != null) {
                throw new RestException(this.getError().getCode(), this.getError().getMessage());
            }
            return result;
        }

        public void setResult(T result) {
            this.result = result;
        }

    }

    public static class Error {

        private Integer code;
        private String message;

        public Error() {

        }

        public Integer getCode() {
            return code;
        }

        public void setCode(Integer code) {
            this.code = code;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

    }

    public static class TypeOf<T> {
        public TypeReference<T> get() {
            return new TypeReference<T>() {};
        }
    }

}
