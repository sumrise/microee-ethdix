package com.microee.ethdix.j3.wss;

import java.util.Arrays;
import java.util.Optional;

public enum ConnectStatus {

    UNKNOW("unknow", "未连接"),
    CONNECTING("connecting", "连接中"),
    ONLINE("online", "已连接"),
    DESTROY("destroy", "主动关闭"), 
    DAMAGED("damaged", "连接损坏,中途断开"), 
    FAILED("failed", "连接失败"), 
    TIMEOUT("timeout", "连接超时"), 
    CLOSED("close", "被动关闭");

    public String code;
    public String desc;

    ConnectStatus(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static ConnectStatus get(final String code) {
        if (code == null || code.trim().isEmpty()) return null;
        Optional<ConnectStatus> o = Arrays.asList(ConnectStatus.values()).stream()
                .filter(p -> p.code.equals(code)).findFirst();
        if (o.isPresent()) {
            return o.get();
        }
        return null;
    }
    
}
