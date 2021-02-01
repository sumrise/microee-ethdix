package com.microee.ethdix.web.actions;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.microee.ethdix.oem.eth.entity.Token;
import com.microee.ethdix.rmi.univ2.ETHUniSwapV2RMi;
import com.microee.plugin.response.R;

// 代币
@RestController
@RequestMapping("/token")
public class TokenRestful {


    @Autowired
    private ETHUniSwapV2RMi univ2RMi;
    
    // ### uniswap v2
    /**
     * 查询默认支持的 token 列表
     * @param chainId
     * @return
     */
    @RequestMapping(value = "/default-token-list", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<List<Token>> defaultTokenList(
            @RequestParam(value = "chainId", required=false, defaultValue="mainnet") String chainId,
            @RequestParam(value = "symbol", required=false) String symbol)  {
        return univ2RMi.defaultTokenList(chainId, symbol);
    }
    
}
