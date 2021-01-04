package com.microee.ethdix.oem.eth;

import java.io.Serializable;
import java.util.List;

public class EthRawBlock implements Serializable {
    
    private static final long serialVersionUID = -1405485144271392436L;

    private String difficulty;
    private String extraData;
    private String gasLimit;
    private String gasUsed;
    private String hash;
    private String logsBloom;
    private String miner;
    private String mixHash;
    private String nonce;
    private String number;
    private String parentHash;
    private String receiptsRoot;
    private String sha3Uncles;
    private String size;
    private String stateRoot;
    private String timestamp;
    private String totalDifficulty;
    private List<EthRawTransaction> transactions;
    private String transactionsRoot;
    private List<String> uncles;
    
    public EthRawBlock() {
        
    }

    public EthRawBlock(String difficulty, String extraData, String gasLimit, String gasUsed,
            String hash, String logsBloom, String miner, String mixHash, String nonce,
            String number, String parentHash, String receiptsRoot, String sha3Uncles, String size,
            String stateRoot, String timestamp, String totalDifficulty,
            List<EthRawTransaction> transactions, String transactionsRoot, List<String> uncles) {
        super();
        this.difficulty = difficulty;
        this.extraData = extraData;
        this.gasLimit = gasLimit;
        this.gasUsed = gasUsed;
        this.hash = hash;
        this.logsBloom = logsBloom;
        this.miner = miner;
        this.mixHash = mixHash;
        this.nonce = nonce;
        this.number = number;
        this.parentHash = parentHash;
        this.receiptsRoot = receiptsRoot;
        this.sha3Uncles = sha3Uncles;
        this.size = size;
        this.stateRoot = stateRoot;
        this.timestamp = timestamp;
        this.totalDifficulty = totalDifficulty;
        this.transactions = transactions;
        this.transactionsRoot = transactionsRoot;
        this.uncles = uncles;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getExtraData() {
        return extraData;
    }

    public void setExtraData(String extraData) {
        this.extraData = extraData;
    }

    public String getGasLimit() {
        return gasLimit;
    }

    public void setGasLimit(String gasLimit) {
        this.gasLimit = gasLimit;
    }

    public String getGasUsed() {
        return gasUsed;
    }

    public void setGasUsed(String gasUsed) {
        this.gasUsed = gasUsed;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getLogsBloom() {
        return logsBloom;
    }

    public void setLogsBloom(String logsBloom) {
        this.logsBloom = logsBloom;
    }

    public String getMiner() {
        return miner;
    }

    public void setMiner(String miner) {
        this.miner = miner;
    }

    public String getMixHash() {
        return mixHash;
    }

    public void setMixHash(String mixHash) {
        this.mixHash = mixHash;
    }

    public String getNonce() {
        return nonce;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getParentHash() {
        return parentHash;
    }

    public void setParentHash(String parentHash) {
        this.parentHash = parentHash;
    }

    public String getReceiptsRoot() {
        return receiptsRoot;
    }

    public void setReceiptsRoot(String receiptsRoot) {
        this.receiptsRoot = receiptsRoot;
    }

    public String getSha3Uncles() {
        return sha3Uncles;
    }

    public void setSha3Uncles(String sha3Uncles) {
        this.sha3Uncles = sha3Uncles;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getStateRoot() {
        return stateRoot;
    }

    public void setStateRoot(String stateRoot) {
        this.stateRoot = stateRoot;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getTotalDifficulty() {
        return totalDifficulty;
    }

    public void setTotalDifficulty(String totalDifficulty) {
        this.totalDifficulty = totalDifficulty;
    }

    public List<EthRawTransaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<EthRawTransaction> transactions) {
        this.transactions = transactions;
    }

    public String getTransactionsRoot() {
        return transactionsRoot;
    }

    public void setTransactionsRoot(String transactionsRoot) {
        this.transactionsRoot = transactionsRoot;
    }

    public List<String> getUncles() {
        return uncles;
    }

    public void setUncles(List<String> uncles) {
        this.uncles = uncles;
    }
    
}
