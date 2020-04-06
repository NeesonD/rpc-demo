package com.neeson.rpc.support.response;

import lombok.Data;

import java.util.Objects;

/**
 * @author daile
 * @version 1.0
 * @date 2020/4/2 22:35
 */
@Data
public class RpcResponse {
    private String requestId;
    private Exception exception;
    private Object result;

    public boolean hasException() {
        if (Objects.nonNull(exception)) {
            return true;
        }
        return false;
    }
}
