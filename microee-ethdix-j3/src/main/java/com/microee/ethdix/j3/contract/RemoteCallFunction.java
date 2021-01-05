package com.microee.ethdix.j3.contract;

import com.microee.plugin.response.R;
import com.microee.plugin.response.exception.RestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.protocol.core.RemoteCall;

public class RemoteCallFunction<T> {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(RemoteCallFunction.class);
    
    private final RemoteCall<T> call;
    
    public RemoteCallFunction(RemoteCall<T> call) {
        this.call = call;
    }
    
    public static RemoteCallFunction<?> build(RemoteCall<?> call) {
        return new RemoteCallFunction<>(call);
    }
    
    @SuppressWarnings({"hiding", "unchecked"})
    public <T> T call() {
        try {
            return (T)this.call.send();
        } catch (Exception e) {
            LOGGER.error("errorMessage={}", e.getMessage(), e);
            throw new RestException(R.FAILED, e.getMessage());
        }
    }
}
