package com.neeson.rpc.anno;


import java.lang.annotation.*;

/**
 * Create on 2020-03-26
 *
 * @author Administrator
 */
@Documented
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface RpcReference {
}
