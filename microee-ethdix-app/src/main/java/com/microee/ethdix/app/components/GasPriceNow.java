package com.microee.ethdix.app.components;

import java.io.Serializable;

import org.json.JSONObject;

import com.fasterxml.jackson.core.type.TypeReference;
import com.microee.plugin.http.assets.HttpAssets;
import com.microee.plugin.http.assets.HttpClient;
import com.microee.plugin.http.assets.HttpClientResult;

public class GasPriceNow implements Serializable {

    private static final String GAS_NOW_URL = "https://gasnow.sparkpool.com/api/v3/gas/price";
    
    private static final long serialVersionUID = 5795681453021516969L;
    
    private Long rapid;
    private Long fast;
    private Long standard;
    private Long slow;
    private Long timestamp;
    
    public GasPriceNow() {
        
    }

    public GasPriceNow(Long rapid, Long fast, Long standard, Long slow, Long timestamp) {
        super();
        this.rapid = rapid;
        this.fast = fast;
        this.standard = standard;
        this.slow = slow;
        this.timestamp = timestamp;
    }

    public Long getRapid() {
        return rapid;
    }

    public void setRapid(Long rapid) {
        this.rapid = rapid;
    }

    public Long getFast() {
        return fast;
    }

    public void setFast(Long fast) {
        this.fast = fast;
    }

    public Long getStandard() {
        return standard;
    }

    public void setStandard(Long standard) {
        this.standard = standard;
    }

    public Long getSlow() {
        return slow;
    }

    public void setSlow(Long slow) {
        this.slow = slow;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
    
    public Long gasPrice(String level) {
        if (level.equals("rapid")) {
            return this.getRapid();
        }
        if (level.equals("fast")) {
            return this.getFast();
        }
        if (level.equals("standard")) {
            return this.getStandard();
        }
        if (level.equals("slow")) {
            return this.getSlow();
        }
        return -1l;
    }
    
    public static GasPriceNow get() {
    	HttpClientResult httpResult = HttpClient.create().doGet(GAS_NOW_URL);
        if (httpResult != null && httpResult.isSuccess()) {
            JSONObject dataJsonObject = new JSONObject(httpResult.getResult());
            JSONObject dataGasPrice = dataJsonObject.getJSONObject("data");
            GasPriceNow now = HttpAssets.parseJson(dataGasPrice.toString(), new TypeReference<GasPriceNow>() {});
            Long rapid = now.getRapid();
            Long fast = now.getFast();
            Long standard = now.getStandard();
            Long slow = now.getSlow();
            Long timestamp = now.getTimestamp();
            GasPriceNow nowNew = new GasPriceNow();
            nowNew.setRapid(rapid);
            nowNew.setFast(fast);
            nowNew.setStandard(standard);
            nowNew.setSlow(slow);
            nowNew.setTimestamp(timestamp);
            return nowNew;
        }
        return null;
    }

}
