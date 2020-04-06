package com.neeson.oss;

import com.neeson.oss.request.OssFileDeleteRequest;
import com.neeson.oss.request.OssTokenRequest;
import com.neeson.oss.response.OssFileDeleteResponse;
import com.neeson.oss.response.OssTokenResponse;
import com.neeson.rpc.anno.ServiceName;

/**
 * Create on 2020-04-03
 *
 * @author Administrator
 */
@ServiceName(value = "oss-service")
public interface IOssRpcService {

    /**
     * 获取OssToken
     * @param request
     * @return
     */
    OssTokenResponse getOssToken(OssTokenRequest request);

    /**
     * 删除oss文件
     * @param request
     * @return
     */
    OssFileDeleteResponse deleteOssFile(OssFileDeleteRequest request);

}
