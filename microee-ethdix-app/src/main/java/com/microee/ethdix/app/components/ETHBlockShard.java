package com.microee.ethdix.app.components;

import org.springframework.stereotype.Component;

@Component
public class ETHBlockShard {

    private static final int _SIZE = 100000; // 每个表分片的大小
    private static final long _MAX = 100000000000000l;
    private static final int _LEFT_COUNT = 12;
    
    public String getCollection(String name, Long blockNumber) {
        for (long i = 0; i<_MAX; i++) {
            long start = i == 0 ? 0 : _SIZE * i + 1;
            long end = i == 0 ? _SIZE : start + _SIZE - 1;
            if (blockNumber >= start && blockNumber <= end) {
                return String.format("%s_%s-%s", name, String.format("%0" + _LEFT_COUNT + "d", start), String.format("%0" + _LEFT_COUNT + "d", end));
            }
        }
        return null;
    }
    
}
