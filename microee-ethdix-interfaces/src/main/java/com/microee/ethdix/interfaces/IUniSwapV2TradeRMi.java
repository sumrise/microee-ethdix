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

    @RequestMapping(value = "/eth2TokenSwapGetParams", method = RequestMethod.POST, consumes=MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<Map<String, Object>> eth2TokenSwapGetParams(
            @RequestParam("tokenAddr") String tokenAddr,
            @RequestBody String eth2TokenSwapVo);

    @RequestMapping(value = "/eth2TokenSwapSendTranaction", method = RequestMethod.POST, consumes=MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<Map<String, Object>> eth2TokenSwapSendTranaction(
            @RequestParam("ethnode") String ethnode,
            @RequestParam("router02Addr") String router02Addr,
            @RequestBody String eth2TokenSwapGetParams);
    
}
