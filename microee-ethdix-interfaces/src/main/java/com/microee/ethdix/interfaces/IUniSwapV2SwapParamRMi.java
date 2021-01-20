package com.microee.ethdix.interfaces;

import java.util.Map;
import org.json.JSONObject;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import com.microee.plugin.response.R;

public interface IUniSwapV2SwapParamRMi {

    @RequestMapping(value = "/eth2token", method = RequestMethod.POST, consumes=MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<Map<String, Object>> eth2token(
            @RequestParam("tokenAddr") String tokenAddr,
            @RequestBody JSONObject params);
    
}
