package com.microee.ethdix.oem.eth.vo.financier;

import java.io.Serializable;
import java.util.List;
import com.microee.ethdix.oem.eth.trans.TransactionTypeEnum;
import com.microee.ethdix.oem.eth.vo.financier.ESEthRawTransaction.TokenTransfer;

public class ESEthRawTransactionVo implements Serializable {

    private static final long serialVersionUID = -9172616508168159958L;

    private String blockHash;
    private Integer transactionIndex;
    private Integer nonce;
    private String input;
    private Long gasUsed;
    private Integer blockNumber;
    private Long gas;
    private String from;
    private String to;
    private String value;
    private String hash;
    private String gasPrice;
    private Integer status;
    private Long timestamp;
    private TransactionTypeEnum transferType;
    private List<TokenTransfer> in;
    private List<TokenTransfer> out;
    
    public ESEthRawTransactionVo() {
        
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

    public TransactionTypeEnum getTransferType() {
        return transferType;
    }

    public void setTransferType(TransactionTypeEnum transferType) {
        this.transferType = transferType;
    }

    public List<TokenTransfer> getIn() {
        return in;
    }

    public void setIn(List<TokenTransfer> in) {
        this.in = in;
    }

    public List<TokenTransfer> getOut() {
        return out;
    }

    public void setOut(List<TokenTransfer> out) {
        this.out = out;
    }
    
}
