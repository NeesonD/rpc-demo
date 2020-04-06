package com.neeson.oss.rpc;

import com.neeson.oss.IOssRpcService;
import com.neeson.oss.request.OssFileDeleteRequest;
import com.neeson.oss.request.OssTokenRequest;
import com.neeson.oss.response.OssFileDeleteResponse;
import com.neeson.oss.response.OssTokenResponse;
import com.neeson.rpc.anno.RpcService;

/**
 * Create on 2020-04-03
 *
 * @author Administrator
 */
@RpcService(IOssRpcService.class)
public class OssRpcService implements IOssRpcService {
    @Override
    public OssTokenResponse getOssToken(OssTokenRequest request) {
        return OssTokenResponse.of("fuckToken");
    }

    @Override
    public OssFileDeleteResponse deleteOssFile(OssFileDeleteRequest request) {
        return OssFileDeleteResponse.of(true);
    }
}
