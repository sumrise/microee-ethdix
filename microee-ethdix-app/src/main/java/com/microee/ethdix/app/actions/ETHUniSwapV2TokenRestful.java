package com.microee.ethdix.app.actions;

import java.util.List;
import org.assertj.core.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.microee.ethdix.app.service.ETHUniSwapV2SDKService;
import com.microee.ethdix.oem.eth.entity.Token;
import com.microee.ethdix.oem.eth.enums.ChainId;
import com.microee.plugin.response.R;

@RestController
@RequestMapping("/univ2-token")
public class ETHUniSwapV2TokenRestful {

    @Autowired
    private ETHUniSwapV2SDKService uniSwapV2SDKService;
    
    /**
     * 查询默认支持的 token 列表
     * @param chainId
     * @return
     */
    @RequestMapping(value = "/default-token-list", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<List<Token>> defaultTokenList(
            @RequestParam(value = "chainId", required=false, defaultValue="mainnet") String chainId,
            @RequestParam(value = "symbol", required=false) String symbol) {
        Assertions.assertThat(ChainId.get(chainId)).withFailMessage("`chainId`有误").isNotNull();
        return R.ok(uniSwapV2SDKService.defaultTokenList(ChainId.get(chainId).code, symbol));
    }
    
}
