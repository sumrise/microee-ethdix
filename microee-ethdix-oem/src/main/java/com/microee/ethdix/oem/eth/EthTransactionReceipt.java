package com.microee.ethdix.oem.eth;

import java.io.Serializable;
import java.util.List;

public class EthTransactionReceipt implements Serializable {

    private static final long serialVersionUID = 257327339325302789L;

    private String blockHash;
    private String blockNumber;
    private String contractAddress;
    private String root;
    private String cumulativeGasUsed;
    private String from;
    private String gasUsed;
    private List<ReceiptLog> logs;
    private String logsBloom;
    private String status;
    private String to;
    private String transactionHash;
    private String transactionIndex;

    public EthTransactionReceipt() {

    }

    public EthTransactionReceipt(String blockHash, String blockNumber, String contractAddress,
            String root, String cumulativeGasUsed, String from, String gasUsed,
            List<ReceiptLog> logs, String logsBloom, String status, String to,
            String transactionHash, String transactionIndex) {
        super();
        this.blockHash = blockHash;
        this.blockNumber = blockNumber;
        this.contractAddress = contractAddress;
        this.root = root;
        this.cumulativeGasUsed = cumulativeGasUsed;
        this.from = from;
        this.gasUsed = gasUsed;
        this.logs = logs;
        this.logsBloom = logsBloom;
        this.status = status;
        this.to = to;
        this.transactionHash = transactionHash;
        this.transactionIndex = transactionIndex;
    }

    public String getBlockHash() {
        return blockHash;
    }

    public void setBlockHash(String blockHash) {
        this.blockHash = blockHash;
    }

    public String getBlockNumber() {
        return blockNumber;
    }

    public void setBlockNumber(String blockNumber) {
        this.blockNumber = blockNumber;
    }

    public String getContractAddress() {
        return contractAddress;
    }

    public void setContractAddress(String contractAddress) {
        this.contractAddress = contractAddress;
    }

    public String getRoot() {
        return root;
    }

    public void setRoot(String root) {
        this.root = root;
    }

    public String getCumulativeGasUsed() {
        return cumulativeGasUsed;
    }

    public void setCumulativeGasUsed(String cumulativeGasUsed) {
        this.cumulativeGasUsed = cumulativeGasUsed;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getGasUsed() {
        return gasUsed;
    }

    public void setGasUsed(String gasUsed) {
        this.gasUsed = gasUsed;
    }

    public List<ReceiptLog> getLogs() {
        return logs;
    }

    public void setLogs(List<ReceiptLog> logs) {
        this.logs = logs;
    }

    public String getLogsBloom() {
        return logsBloom;
    }

    public void setLogsBloom(String logsBloom) {
        this.logsBloom = logsBloom;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getTransactionHash() {
        return transactionHash;
    }

    public void setTransactionHash(String transactionHash) {
        this.transactionHash = transactionHash;
    }

    public String getTransactionIndex() {
        return transactionIndex;
    }

    public void setTransactionIndex(String transactionIndex) {
        this.transactionIndex = transactionIndex;
    }

    public static class ReceiptLog implements Serializable {

        private static final long serialVersionUID = 5471475534197698123L;

        private boolean removed;
        private String logIndex;
        private String transactionIndex;
        private String transactionHash;
        private String blockHash;
        private String blockNumber;
        private String address;
        private String data;
        private String type;
        private List<String> topics;

        public ReceiptLog() {

        }

        public ReceiptLog(boolean removed, String logIndex, String transactionIndex,
                String transactionHash, String blockHash, String blockNumber, String address,
                String data, String type, List<String> topics) {
            super();
            this.removed = removed;
            this.logIndex = logIndex;
            this.transactionIndex = transactionIndex;
            this.transactionHash = transactionHash;
            this.blockHash = blockHash;
            this.blockNumber = blockNumber;
            this.address = address;
            this.data = data;
            this.type = type;
            this.topics = topics;
        }

        public boolean isRemoved() {
            return removed;
        }

        public void setRemoved(boolean removed) {
            this.removed = removed;
        }

        public String getLogIndex() {
            return logIndex;
        }

        public void setLogIndex(String logIndex) {
            this.logIndex = logIndex;
        }

        public String getTransactionIndex() {
            return transactionIndex;
        }

        public void setTransactionIndex(String transactionIndex) {
            this.transactionIndex = transactionIndex;
        }

        public String getTransactionHash() {
            return transactionHash;
        }

        public void setTransactionHash(String transactionHash) {
            this.transactionHash = transactionHash;
        }

        public String getBlockHash() {
            return blockHash;
        }

        public void setBlockHash(String blockHash) {
            this.blockHash = blockHash;
        }

        public String getBlockNumber() {
            return blockNumber;
        }

        public void setBlockNumber(String blockNumber) {
            this.blockNumber = blockNumber;
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

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public List<String> getTopics() {
            return topics;
        }

        public void setTopics(List<String> topics) {
            this.topics = topics;
        }

    }

}
