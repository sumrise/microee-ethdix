package com.microee.ethdix.app.props;

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

    
}
