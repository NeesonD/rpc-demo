package com.neeson.rpc.support;

import com.neeson.rpc.client.RpcProxy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.cglib.core.SpringNamingPolicy;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Method;

/**
 * @author daile
 * @version 1.0
 * @date 2020/4/3 22:05
 */
@Slf4j
public class RpcReferenceFactoryBean<T> implements FactoryBean<T> {

    private String innerClassName;
    private RpcProxy rpcProxy;

    public RpcReferenceFactoryBean(String innerClassName, BeanFactory beanFactory) {
        this.innerClassName = innerClassName;
        this.rpcProxy = beanFactory.getBean(RpcProxy.class);
    }

    @Override
    public T getObject() throws ClassNotFoundException {
        Class innerClass = Class.forName(innerClassName);
        if (innerClass.isInterface()) {
            return (T) rpcProxy.create(innerClass);
        } else {
            Enhancer enhancer = new Enhancer();
            enhancer.setSuperclass(innerClass);
            enhancer.setNamingPolicy(SpringNamingPolicy.INSTANCE);
            enhancer.setCallback(new MethodInterceptorImpl());
            return (T) enhancer.create();
        }
    }

    @Override
    public Class<?> getObjectType() {
        try {
            return Class.forName(innerClassName, true, Thread.currentThread().getContextClassLoader());
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }


    public static class MethodInterceptorImpl implements MethodInterceptor {
        @Override
        public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
            System.out.println("MethodInterceptorImpl:" + method.getName());
            return methodProxy.invokeSuper(o, objects);
        }
    }

}
