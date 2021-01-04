package com.microee.ethdix.oem.eth.vo.financier;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import com.microee.ethdix.oem.eth.constrans.ETHContractInput;
import com.microee.ethdix.oem.eth.contract.ERC20ContractInfo;
import com.microee.ethdix.oem.eth.trans.TransactionTypeEnum;
import com.microee.ethdix.oem.eth.vo.financier.ESEthRawTransaction.Token;

public class ESEthRawTransaction implements Serializable {

    private static final long serialVersionUID = 2491881816605165337L;

    private String blockHash;
    private Integer transactionIndex;
    private Integer nonce;
    private String input;
    private Long gasUsed;
    private List<TokenTransfer> tokenTransfer;
    private Integer blockNumber;
    private Long gas;
    private String from;
    private String to;
    private String value;
    private List<Logs> logs;
    private String hash;
    private String gasPrice;
    private Integer status;
    private Long timestamp;

    public ESEthRawTransaction() {

    }

    public ESEthRawTransaction(String blockHash, Integer transactionIndex, Integer nonce,
            String input, Long gasUsed, List<TokenTransfer> tokenTransfer, Integer blockNumber,
            Long gas, String from, String to, String value, List<Logs> logs, String hash,
            String gasPrice, Integer status, Long timestamp) {
        super();
        this.blockHash = blockHash;
        this.transactionIndex = transactionIndex;
        this.nonce = nonce;
        this.input = input;
        this.gasUsed = gasUsed;
        this.tokenTransfer = tokenTransfer;
        this.blockNumber = blockNumber;
        this.gas = gas;
        this.from = from;
        this.to = to;
        this.value = value;
        this.logs = logs;
        this.hash = hash;
        this.gasPrice = gasPrice;
        this.status = status;
        this.timestamp = timestamp;
    }
    
    public TransactionTypeEnum transferType (String address, boolean isContractForToAddress) {
        String from = this.getFrom();
        List<TokenTransfer> tokenTransferList = this.getTokenTransfer();
        TransactionTypeEnum transType = TransactionTypeEnum.TRANSFER;
        if (!isContractForToAddress) {
            return transType;
        }
        // to地址为合约地址
        String input = this.getInput();
        if (tokenTransferList == null || tokenTransferList.size() == 0) {
            // 没有erc20代币转移时，判断是否为授权，授权以外的操作一律为执行合约
            if (input != null && input.startsWith(ETHContractInput.APPROVE_METHOD_ID)) {
                transType = TransactionTypeEnum.APPROVE;
            } else {
                transType = TransactionTypeEnum.CONTRACT;
            }
            return transType;
        } 
        // 普通转账
        if (input != null && input.startsWith(ETHContractInput.TRANSFER_METHOD_ID)) {
            if (address.equalsIgnoreCase(from)) {
                transType = TransactionTypeEnum.TRANSFER; // 转出
            } else {
                transType = TransactionTypeEnum.RECEIPT; // 转入
            }
            return transType;
        } 
        // 有多笔 erc20 转移时，有三种情况：
        // 1.若同一地址既存在转出与转入，则视为交易类型;
        // 2.若只存在转入且判断地址与发起交易地址不同，视为收款;
        // 3.其他情况，视为执行合约
        boolean out = false;
        boolean in = false;
        for (TokenTransfer tokenTransfer : tokenTransferList) {
            String tokenFrom = tokenTransfer.getFrom();
            String tokenTo = tokenTransfer.getTo();
            if (tokenFrom.equalsIgnoreCase(from)) {
                out = true;
                continue;
            }
            if (tokenTo.equalsIgnoreCase(address)) {
                in = true;
            }
        }
        if (out && in) {
            transType = TransactionTypeEnum.SWAP;
        } else if (in && !address.equalsIgnoreCase(from)) {
            transType = TransactionTypeEnum.RECEIPT;
        } else {
            transType = TransactionTypeEnum.CONTRACT;
        }
        return transType;
    }

    public String getBlockHash() {
        return blockHash;
    }

    public void setBlockHash(String blockHash) {
        this.blockHash = blockHash;
    }

    public Integer getTransactionIndex() {
        return transactionIndex;
    }

    public void setTransactionIndex(Integer transactionIndex) {
        this.transactionIndex = transactionIndex;
    }

    public Integer getNonce() {
        return nonce;
    }

    public void setNonce(Integer nonce) {
        this.nonce = nonce;
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public Long getGasUsed() {
        return gasUsed;
    }

    public void setGasUsed(Long gasUsed) {
        this.gasUsed = gasUsed;
    }

    public List<TokenTransfer> getTokenTransfer() {
        return tokenTransfer;
    }

    public void setTokenTransfer(List<TokenTransfer> tokenTransfer) {
        this.tokenTransfer = tokenTransfer;
    }

    public Integer getBlockNumber() {
        return blockNumber;
    }

    public void setBlockNumber(Integer blockNumber) {
        this.blockNumber = blockNumber;
    }

    public Long getGas() {
        return gas;
    }

    public void setGas(Long gas) {
        this.gas = gas;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public List<Logs> getLogs() {
        return logs;
    }

    public void setLogs(List<Logs> logs) {
        this.logs = logs;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getGasPrice() {
        return gasPrice;
    }

    public void setGasPrice(String gasPrice) {
        this.gasPrice = gasPrice;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
    
    public static class Token implements Serializable {
        
        private static final long serialVersionUID = 1548034507549866438L;

        private Double amount;
        private String symbol;
        private Double price;
        private Double value;
        private String icon;
        private String address;
        
        public Token() {
            
        }
        
        public Token(Double price) {
            this.price = price;
        }

        public Double getAmount() {
            return amount;
        }

        public void setAmount(Double amount) {
            this.amount = amount;
        }

        public String getSymbol() {
            return symbol;
        }

        public void setSymbol(String symbol) {
            this.symbol = symbol;
        }

        public Double getPrice() {
            return price;
        }

        public void setPrice(Double price) {
            this.price = price;
        }

        public Double getValue() {
            return value;
        }

        public void setValue(Double value) {
            this.value = value;
        }

        public String getIcon() {
            return icon;
        }

        public void setIcon(String icon) {
            this.icon = icon;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }
        
    }

    public static class TokenTransfer implements Serializable {

        private static final long serialVersionUID = -4240570326283136772L;

        private String tokenAddress;
        private String from;
        private String to;
        private String value;
        private Token token;

        public TokenTransfer() {

        }

        public TokenTransfer(String tokenAddress, String from, String to, String value) {
            super();
            this.tokenAddress = tokenAddress;
            this.from = from;
            this.to = to;
            this.value = value;
        }
        
        public static TokenTransfer build(String from, String to, String value, int precision, Double price) {
            TokenTransfer transfer = new TokenTransfer();
            transfer.setFrom(from);
            transfer.setTo(to);
            transfer.setValue(value);
            Token token = new Token(price);
            token.setAmount(Double.parseDouble(value));
            token.setValue(BigDecimal.valueOf(token.getAmount() * token.getPrice()).setScale(precision, RoundingMode.HALF_UP).doubleValue());
            transfer.setToken(token);
            return transfer;
        }

        public static TokenTransfer build(String from, String to, String value, String tokenAddress, ERC20ContractInfo erc20ContractInfo) {
            TokenTransfer transfer = new TokenTransfer();
            transfer.setFrom(from);
            transfer.setTo(to);
            transfer.setValue(value);
            if (erc20ContractInfo != null) {
                Token token = new Token(erc20ContractInfo.getPrice());
                token.setSymbol(erc20ContractInfo.getSymbol());
                token.setAddress(tokenAddress);
                transfer.setToken(token);
            }
            return transfer;
        }

        public String getTokenAddress() {
            return tokenAddress;
        }

        public void setTokenAddress(String tokenAddress) {
            this.tokenAddress = tokenAddress;
        }

        public String getFrom() {
            return from;
        }

        public void setFrom(String from) {
            this.from = from;
        }

        public String getTo() {
            return to;
        }

        public void setTo(String to) {
            this.to = to;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public Token getToken() {
            return token;
        }

        public void setToken(Token token) {
            this.token = token;
        }

    }

    public static class Logs implements Serializable {

        private static final long serialVersionUID = -8475547396627457080L;

        private String address;
        private String data;
        private List<String> topics;

        public Logs() {

        }

        public Logs(String address, String data, List<String> topics) {
            super();
            this.address = address;
            this.data = data;
            this.topics = topics;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getData() {
            return data;
        }

        public void setData(String data) {
            this.data = data;
        }

        public List<String> getTopics() {
            return topics;
        }

        public void setTopics(List<String> topics) {
            this.topics = topics;
        }

    }

}
