package com.neeson.oss;

import com.neeson.rpc.anno.RpcScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Create on 2020-04-03
 *
 * @author Administrator
 */
@SpringBootApplication
@RpcScan
public class OssApplication {

    public static void main(String[] args) {
        SpringApplication.run(OssApplication.class, args);
    }

}
