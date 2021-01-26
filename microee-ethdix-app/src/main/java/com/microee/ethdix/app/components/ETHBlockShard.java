package com.microee.ethdix.app.components;

import java.util.Locale;
import org.springframework.stereotype.Component;
import com.microee.ethdix.oem.eth.enums.ChainId;

@Component
public class ETHBlockShard {

    private static final int _SIZE = 100000; // 每个表分片的大小
    private static final long _MAX = 100000000000000l;
    private static final int _LEFT_COUNT = 12;
    private static final String _PREFIX = "eth_";
    
    public String getCollection(ChainId chainId, String name, Long blockNumber) {
        for (long i = 0; i<_MAX; i++) {
            long start = i == 0 ? 0 : _SIZE * i + 1;
            long end = i == 0 ? _SIZE : start + _SIZE - 1;
            if (blockNumber >= start && blockNumber <= end) {
                return String.format("%s%s_%s_%s-%s", _PREFIX, chainId.name.toLowerCase(Locale.getDefault()), name, String.format("%0" + _LEFT_COUNT + "d", start), String.format("%0" + _LEFT_COUNT + "d", end));
            }
        }
        return null;
    }
    
}
