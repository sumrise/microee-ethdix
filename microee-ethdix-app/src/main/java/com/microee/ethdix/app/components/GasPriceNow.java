package com.microee.ethdix.app.components;

import java.io.Serializable;

import org.json.JSONObject;

import com.fasterxml.jackson.core.type.TypeReference;
import com.microee.plugin.http.assets.HttpAssets;
import com.microee.plugin.http.assets.HttpClient;
import com.microee.plugin.http.assets.HttpClientResult;
import com.microee.plugin.response.R;
import com.microee.plugin.response.exception.RestException;

public class GasPriceNow implements Serializable {

    private static final String GAS_NOW_URL = "https://gasnow.sparkpool.com/api/v3/gas/price";

    private static final long serialVersionUID = 5795681453021516969L;

    private Double rapid;
    private Double fast;
    private Double standard;
    private Double slow;
    private Double timestamp;

    public GasPriceNow() {

    }

    public GasPriceNow(Double rapid, Double fast, Double standard, Double slow, Double timestamp) {
        super();
        this.rapid = rapid;
        this.fast = fast;
        this.standard = standard;
        this.slow = slow;
        this.timestamp = timestamp;
    }

    public Double getRapid() {
        return rapid;
    }

    public void setRapid(Double rapid) {
        this.rapid = rapid;
    }

    public Double getFast() {
        return fast;
    }

    public void setFast(Double fast) {
        this.fast = fast;
    }

    public Double getStandard() {
        return standard;
    }

    public void setStandard(Double standard) {
        this.standard = standard;
    }

    public Double getSlow() {
        return slow;
    }

    public void setSlow(Double slow) {
        this.slow = slow;
    }

    public Double getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Double timestamp) {
        this.timestamp = timestamp;
    }

    public Double gasPrice(String level) {
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
        return this.getSlow();
    }

    public static GasPriceNow get() {
        HttpClientResult httpResult = HttpClient.create().doGet(GAS_NOW_URL);
        if (httpResult == null || !httpResult.isSuccess()) {
            throw new RestException(R.FAILED, httpResult == null ? "获取gas价格失败" : httpResult.getMessage());
        }
        JSONObject dataJsonObject = new JSONObject(httpResult.getResult());
        JSONObject dataGasPrice = dataJsonObject.getJSONObject("data");
        GasPriceNow now = HttpAssets.parseJson(dataGasPrice.toString(), new TypeReference<GasPriceNow>() {});
        Double rapid = now.getRapid();
        Double fast = now.getFast();
        Double standard = now.getStandard();
        Double slow = now.getSlow();
        Double timestamp = now.getTimestamp();
        GasPriceNow nowNew = new GasPriceNow();
        nowNew.setRapid(rapid);
        nowNew.setFast(fast);
        nowNew.setStandard(standard);
        nowNew.setSlow(slow);
        nowNew.setTimestamp(timestamp);
        return nowNew;
    }

}
