package com.neeson.rpc.client;

import com.neeson.rpc.anno.ServiceName;
import com.neeson.rpc.support.request.RpcRequest;
import com.neeson.rpc.support.response.RpcResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cglib.proxy.Proxy;

import java.lang.reflect.Method;
import java.util.UUID;

/**
 * @author daile
 * @version 1.0
 * @date 2020/4/2 23:31
 */
@Slf4j
public class RpcProxy {

    private ServiceDiscovery serviceDiscovery;

    public RpcProxy(ServiceDiscovery serviceDiscovery) {
        this.serviceDiscovery = serviceDiscovery;
    }

    @SuppressWarnings("unchecked")
    public <T> T create(Class<?> interfaceClass) {
        return (T) Proxy.newProxyInstance(
                interfaceClass.getClassLoader(),
                new Class<?>[]{interfaceClass},
                (proxy, method, args) -> {
                    RpcRequest request = getRpcRequest(method, args);

                    RpcClient client = getRpcClient(method);

                    long time = System.currentTimeMillis();
                    RpcResponse response = client.send(request,client);
                    log.debug("time: {}ms", System.currentTimeMillis() - time);
                    checkRpcResponse(response);
                    return response.getResult();
                }
        );
    }

    private void checkRpcResponse(RpcResponse response) throws Exception {
        if (response == null) {
            throw new RuntimeException("response is null");
        }
        if (response.hasException()) {
            throw response.getException();
        }
    }

    private RpcClient getRpcClient(Method method) {
        String serviceName = method.getDeclaringClass().getAnnotation(ServiceName.class).value();

        // 发现服务
        String serverAddress = serviceDiscovery.discover(serviceName);

        String[] array = serverAddress.split(":");
        String host = array[0];
        int port = Integer.parseInt(array[1]);

        return new RpcClient(host, port);
    }

    private RpcRequest getRpcRequest(Method method, Object[] args) {
        return RpcRequest.builder()
                .interfaceName(method.getDeclaringClass().getName())
                .methodName(method.getName())
                .parameters(args)
                .parameterTypes(method.getParameterTypes())
                .requestId(UUID.randomUUID().toString())
                .build();
    }

}
