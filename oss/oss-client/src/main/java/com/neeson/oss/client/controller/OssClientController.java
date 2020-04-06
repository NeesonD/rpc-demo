package com.neeson.oss.client.controller;

import com.neeson.oss.IOssRpcService;
import com.neeson.oss.request.OssFileDeleteRequest;
import com.neeson.oss.request.OssTokenRequest;
import com.neeson.oss.response.OssFileDeleteResponse;
import com.neeson.oss.response.OssTokenResponse;
import com.neeson.rpc.anno.RpcReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Create on 2020-04-03
 *
 * @author Administrator
 */
@RestController
@RequestMapping("OssClientController")
public class OssClientController {

    @RpcReference
    private IOssRpcService ossRpcService;

    @GetMapping("getOssToken")
    public OssTokenResponse getOssToken() {
        return ossRpcService.getOssToken(OssTokenRequest.of("1"));
    }

    @GetMapping("deleteOssFile")
    public OssFileDeleteResponse deleteOssFile() {
        return ossRpcService.deleteOssFile(OssFileDeleteRequest.of("1"));
    }

}
