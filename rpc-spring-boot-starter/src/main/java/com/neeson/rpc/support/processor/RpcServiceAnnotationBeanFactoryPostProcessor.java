package com.neeson.rpc.support.processor;

import com.neeson.rpc.support.RpcServiceClassPathBeanDefinitionScanner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.SingletonBeanRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.context.annotation.AnnotationBeanNameGenerator;
import org.springframework.context.annotation.AnnotationConfigUtils;
import org.springframework.context.annotation.ConfigurationClassPostProcessor;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.springframework.context.annotation.AnnotationConfigUtils.CONFIGURATION_BEAN_NAME_GENERATOR;

/**
 * @author daile
 * @version 1.0
 * @date 2020/4/3 20:48
 */
@Slf4j
public class RpcServiceAnnotationBeanFactoryPostProcessor implements BeanDefinitionRegistryPostProcessor {

    private final Set<String> packagesToScan;

    public RpcServiceAnnotationBeanFactoryPostProcessor(String... packagesToScan) {
        this(Arrays.asList(packagesToScan));
    }

    public RpcServiceAnnotationBeanFactoryPostProcessor(Collection<String> packagesToScan) {
        this(new LinkedHashSet<>(packagesToScan));
    }

    public RpcServiceAnnotationBeanFactoryPostProcessor(Set<String> packagesToScan) {
        this.packagesToScan = packagesToScan;
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        Set<String> resolvedPackagesToScan = resolvePackagesToScan(packagesToScan);
        if (!CollectionUtils.isEmpty(resolvedPackagesToScan)) {
            registerRpcServiceBeans(resolvedPackagesToScan, registry);
        } else {
            if (log.isWarnEnabled()) {
                log.warn("packagesToScan is empty , ServiceBean registry will be ignored!");
            }
        }
    }

    private Set<String> resolvePackagesToScan(Set<String> packagesToScan) {
        Set<String> resolvedPackagesToScan = new LinkedHashSet<>(packagesToScan.size());
        for (String packageToScan : packagesToScan) {
            if (StringUtils.hasText(packageToScan)) {
                resolvedPackagesToScan.add(packageToScan);
            }
        }
        return resolvedPackagesToScan;
    }


    private void registerRpcServiceBeans(Set<String> packagesToScan, BeanDefinitionRegistry registry) {
        RpcServiceClassPathBeanDefinitionScanner scanner =
                new RpcServiceClassPathBeanDefinitionScanner(registry);

        BeanNameGenerator beanNameGenerator = resolveBeanNameGenerator(registry);

        scanner.setBeanNameGenerator(beanNameGenerator);

        for (String packageToScan : packagesToScan) {

            // Registers @RpcService Bean
            scanner.scan(packageToScan);

        }

    }

    /**
     * It'd better to use BeanNameGenerator instance that should reference
     * {@link ConfigurationClassPostProcessor#componentScanBeanNameGenerator},
     * thus it maybe a potential problem on bean name generation.
     *
     * @param registry {@link BeanDefinitionRegistry}
     * @return {@link BeanNameGenerator} instance
     * @see SingletonBeanRegistry
     * @see AnnotationConfigUtils#CONFIGURATION_BEAN_NAME_GENERATOR
     * @see ConfigurationClassPostProcessor#processConfigBeanDefinitions
     * @since 2.5.8
     */
    private BeanNameGenerator resolveBeanNameGenerator(BeanDefinitionRegistry registry) {

        BeanNameGenerator beanNameGenerator = null;

        if (registry instanceof SingletonBeanRegistry) {
            SingletonBeanRegistry singletonBeanRegistry = SingletonBeanRegistry.class.cast(registry);
            beanNameGenerator = (BeanNameGenerator) singletonBeanRegistry.getSingleton(CONFIGURATION_BEAN_NAME_GENERATOR);
        }

        if (beanNameGenerator == null) {

            if (log.isInfoEnabled()) {

                log.info("BeanNameGenerator bean can't be found in BeanFactory with name ["
                        + CONFIGURATION_BEAN_NAME_GENERATOR + "]");
                log.info("BeanNameGenerator will be a instance of " +
                        AnnotationBeanNameGenerator.class.getName() +
                        " , it maybe a potential problem on bean name generation.");
            }

            beanNameGenerator = new AnnotationBeanNameGenerator();

        }

        return beanNameGenerator;

    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException {

    }
}
