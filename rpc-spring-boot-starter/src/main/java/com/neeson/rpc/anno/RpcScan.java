package com.neeson.rpc.anno;

import com.neeson.rpc.support.RpcComponentScanRegistrar;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * @author daile
 * @version 1.0
 * @date 2020/4/3 22:10
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(RpcComponentScanRegistrar.class)
public @interface RpcScan {

    @AliasFor("basePackages")
    String[] value() default {};

    @AliasFor("value")
    String[] basePackages() default {};

    Class<?>[] basePackageClasses() default {};

}
