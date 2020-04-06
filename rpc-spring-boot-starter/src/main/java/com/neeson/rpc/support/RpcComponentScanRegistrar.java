package com.neeson.rpc.support;

import com.neeson.rpc.anno.RpcReference;
import com.neeson.rpc.anno.RpcScan;
import com.neeson.rpc.anno.RpcService;
import com.neeson.rpc.support.processor.RpcReferenceAnnotationBeanPostProcessor;
import com.neeson.rpc.support.processor.RpcServiceAnnotationBeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.*;
import org.springframework.boot.autoconfigure.domain.EntityScanPackages;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.util.*;

import static org.springframework.beans.factory.support.BeanDefinitionBuilder.rootBeanDefinition;

/**
 * @author daile
 * @version 1.0
 * @date 2020/4/3 22:51
 *
 * @see RpcScan
 * @see RpcService
 * @see RpcReference
 * @see RpcServiceAnnotationBeanFactoryPostProcessor
 * @see RpcReferenceAnnotationBeanPostProcessor
 */
public class RpcComponentScanRegistrar implements ImportBeanDefinitionRegistrar {


    @Override
    public void registerBeanDefinitions(AnnotationMetadata metadata,
                                        BeanDefinitionRegistry registry) {
        Set<String> packagesToScan = getPackagesToScan(metadata);
        registerRpcServiceAnnotationBeanFactoryPostProcessor(registry, packagesToScan);

        registerRpcReferenceAnnotationBeanPostProcessor(registry);

    }



    /**
     * @see EntityScanPackages.Registrar#getPackagesToScan(AnnotationMetadata)
     * @see DubboComponentScanRegistrar
     * @param metadata
     * @return
     * 就是获取包名，scan 后缀的基本都是这样
     */
    private Set<String> getPackagesToScan(AnnotationMetadata metadata) {
        AnnotationAttributes attributes = AnnotationAttributes.fromMap(
                metadata.getAnnotationAttributes(RpcScan.class.getName()));
        String[] basePackages = attributes.getStringArray("basePackages");
        Class<?>[] basePackageClasses = attributes
                .getClassArray("basePackageClasses");
        String[] value = attributes.getStringArray("value");
        Set<String> packagesToScan = new LinkedHashSet<>(Arrays.asList(value));
        packagesToScan.addAll(Arrays.asList(basePackages));
        for (Class<?> basePackageClass : basePackageClasses) {
            packagesToScan.add(ClassUtils.getPackageName(basePackageClass));
        }
        if (packagesToScan.isEmpty()) {
            String packageName = ClassUtils.getPackageName(metadata.getClassName());
            Assert.state(!StringUtils.isEmpty(packageName),
                    "@RpcService cannot be used with the default package");
            return Collections.singleton(packageName);
        }
        return packagesToScan;
    }

    /**
     * Register the specified entity scan packages with the system.
     * 这里将包名和 RpcServiceAnnotationBeanPostProcessor 关联起来了，给实现类用的
     * @param registry     the source registry
     * @param packageNames the package names to register
     */
    private void registerRpcServiceAnnotationBeanFactoryPostProcessor(BeanDefinitionRegistry registry,
                          Collection<String> packageNames) {
        Assert.notNull(registry, "Registry must not be null");
        Assert.notNull(packageNames, "PackageNames must not be null");
        BeanDefinitionBuilder builder = rootBeanDefinition(RpcServiceAnnotationBeanFactoryPostProcessor.class);
        builder.addConstructorArgValue(packageNames);
        builder.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
        AbstractBeanDefinition beanDefinition = builder.getBeanDefinition();
        BeanDefinitionReaderUtils.registerWithGeneratedName(beanDefinition, registry);
    }

    /**
     * 给接口代理用的
     * @param registry
     */
    private void registerRpcReferenceAnnotationBeanPostProcessor(BeanDefinitionRegistry registry) {
        // Register @RpcReference Annotation Bean Processor
        if (!registry.containsBeanDefinition(RpcReferenceAnnotationBeanPostProcessor.BEAN_NAME)) {
            RootBeanDefinition beanDefinition = new RootBeanDefinition(RpcReferenceAnnotationBeanPostProcessor.class);
            beanDefinition.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
            registry.registerBeanDefinition(RpcReferenceAnnotationBeanPostProcessor.BEAN_NAME, beanDefinition);
        }

    }
}
