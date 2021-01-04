package com.microee.ethdix.app.props;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "eth")
public class ETHNetworkProperties {

    private List<String> mainnetNodes;
    private List<String> ropstenNodes;
    private String mainnetWss;
    private String ropstenWss;
    private List<String> collections;
    
    public ETHNetworkProperties() {
        
    }

    public List<String> getMainnetNodes() {
        return mainnetNodes;
    }
    
    public String[] mainnet() {
    	return this.getMainnetNodes().toArray(new String[this.getMainnetNodes().size()]);
    }
    
    public String[] ropsten() {
    	return this.getRopstenNodes().toArray(new String[this.getRopstenNodes().size()]);
    }

    public void setMainnetNodes(List<String> mainnetNodes) {
        this.mainnetNodes = mainnetNodes;
    }

    public List<String> getRopstenNodes() {
        return ropstenNodes;
    }

    public void setRopstenNodes(List<String> ropstenNodes) {
        this.ropstenNodes = ropstenNodes;
    }

    public String getMainnetWss() {
        return mainnetWss;
    }

    public void setMainnetWss(String mainnetWss) {
        this.mainnetWss = mainnetWss;
    }

    public String getRopstenWss() {
		return ropstenWss;
	}

	public void setRopstenWss(String ropstenWss) {
		this.ropstenWss = ropstenWss;
	}

	public List<String> getCollections() {
        return collections;
    }

    public void setCollections(List<String> collections) {
        this.collections = collections;
    }
    
    public String getCollectionNameByBlockNumber(Long blockNumber) {
        List<String> result = getCollectionNamesByBlockNumber(blockNumber);
        return result.size() > 0 ? result.get(0) : null;
    }
    
    public List<String> getCollectionNamesByBlockNumber(Long blockNumber) {
        List<String> result = new ArrayList<>();
        for (String s : collections) {
            Long start = Long.parseLong(s.split("-")[0]);
            Long end = Long.parseLong(s.split("-")[1]);
            if (blockNumber != null) {
                if (blockNumber >= start && blockNumber <= end) {
                    result.add(String.format("%09d", start) + "-" + String.format("%09d", end));
                    return result;
                } 
            } else {
                result.add(String.format("%09d", start) + "-" + String.format("%09d", end));
            }
        }
        return result;
    }

    public String getCollectionName(String collectionName, Long blockNumber) {
        String collectionNameShardKey = this.getCollectionNameByBlockNumber(blockNumber);
        if (collectionName != null) {
            return collectionName + "_" + collectionNameShardKey;
        }
        return null;
    }


    public String getCollectionName(String collectionName, String collectionNameShardKey) {
        return collectionName + "_" + collectionNameShardKey;
    }
    
}
