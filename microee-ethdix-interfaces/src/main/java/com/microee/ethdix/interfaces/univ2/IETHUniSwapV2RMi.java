package com.microee.ethdix.interfaces.univ2;

import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import com.microee.ethdix.oem.eth.entity.Token;
import com.microee.plugin.response.R;

public interface IETHUniSwapV2RMi {

    /**
     * 查询默认支持的 token 列表
     * @param chainId
     * @return
     */
    @RequestMapping(value = "/default-token-list", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<List<Token>> defaultTokenList(
            @RequestParam(value = "chainId", required=false, defaultValue="mainnet") String chainId,
            @RequestParam(value = "symbol", required=false) String symbol) ;
}
