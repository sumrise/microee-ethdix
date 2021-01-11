package com.microee.ethdix.app.actions;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.microee.ethdix.app.service.ETHUniSwapV2TradeService;
import com.microee.plugin.response.R;

@RestController
@RequestMapping("/univ2-trade")
public class ETHUniSwapV2TradeRestful {
    
    @Autowired
    private ETHUniSwapV2TradeService uniSwapV2TradeService;

    @RequestMapping(value = "/eth2TokenSwap", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<Map<String, Object>> eth2TokenSwap(
            @RequestParam("tokenAddr") String tokenAddr,
            @RequestBody String eth2TokenSwapVo) {
        R<Map<String, Object>> eth2TokenSwapResult = uniSwapV2TradeService.eth2TokenSwap(tokenAddr, eth2TokenSwapVo);
        return R.ok(eth2TokenSwapResult.getData()).message(eth2TokenSwapResult.getMessage());
    }
    
}
