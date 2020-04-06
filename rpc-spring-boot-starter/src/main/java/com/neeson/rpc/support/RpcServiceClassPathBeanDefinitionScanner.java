package com.neeson.rpc.support;

import com.neeson.rpc.anno.RpcService;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.filter.AnnotationTypeFilter;

/**
 * @author daile
 * @version 1.0
 * @date 2020/4/3 22:10
 */
public class RpcServiceClassPathBeanDefinitionScanner extends ClassPathBeanDefinitionScanner {

    public RpcServiceClassPathBeanDefinitionScanner(BeanDefinitionRegistry registry) {
        super(registry);
    }

    @Override
    public void registerDefaultFilters() {
        this.addIncludeFilter(new AnnotationTypeFilter(RpcService.class));
    }


    @Override
    public boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
        return super.isCandidateComponent(beanDefinition) || beanDefinition.getMetadata()
                .hasAnnotation(RpcService.class.getName());
    }
}
