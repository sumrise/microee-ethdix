package com.microee.ethdix.interfaces;

import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import com.microee.plugin.response.R;

// UniSwapV2 Trade RMi
public interface IUniSwapV2TradeRMi {

    @RequestMapping(value = "/eth2TokenSwap", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<Map<String, Object>> eth2TokenSwap(
            @RequestParam("tokenAddr") String tokenAddr,
            @RequestBody String eth2TokenSwapVo);
    
}
