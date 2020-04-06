package com.neeson.rpc.support.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author daile
 * @version 1.0
 * @date 2020/4/2 22:34
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RpcRequest {

    private String requestId;
    private String interfaceName;
    private String methodName;
    private Class<?>[] parameterTypes;
    private Object[] parameters;


}
