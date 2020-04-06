package com.neeson.rpc.support.processor;

import com.neeson.rpc.anno.RpcReference;
import com.neeson.rpc.support.RpcReferenceFactoryBean;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.InjectionMetadata;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.lang.Nullable;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author daile
 * @version 1.0
 * @date 2020/4/4 13:00
 * <p>
 * 这里的设计建议去看 Dubbo 上的 AbstractAnnotationBeanPostProcessor
 */
public class RpcReferenceAnnotationBeanPostProcessor implements InstantiationAwareBeanPostProcessor, BeanFactoryAware {

    public static final String BEAN_NAME = "rpcReferenceAnnotationBeanPostProcessor";

    private final Set<Class<? extends Annotation>> annotationTypes =
            new LinkedHashSet<Class<? extends Annotation>>();

    private final Map<String, InjectionMetadata> injectionMetadataCache = new ConcurrentHashMap<>(256);

    private final ConcurrentMap<String, Object> injectedObjectsCache = new ConcurrentHashMap<>(256);

    private final ConcurrentMap<String, RpcReferenceFactoryBean<?>> referenceBeanCache = new ConcurrentHashMap<>(256);

    private final ConcurrentMap<InjectionMetadata.InjectedElement, RpcReferenceFactoryBean<?>> injectedFieldReferenceBeanCache =
            new ConcurrentHashMap<>(256);


    private ConfigurableListableBeanFactory beanFactory;


    public RpcReferenceAnnotationBeanPostProcessor() {
        this.annotationTypes.add(RpcReference.class);
    }


    @Override
    public PropertyValues postProcessProperties(PropertyValues pvs, Object bean, String beanName) throws BeansException {
        InjectionMetadata metadata = findInjectionMetadata(beanName, bean.getClass(), pvs);
        try {
            metadata.inject(bean, beanName, pvs);
        } catch (BeanCreationException ex) {
            throw ex;
        } catch (Throwable ex) {
            throw new BeanCreationException(beanName, "Injection of autowired dependencies failed", ex);
        }
        return pvs;
    }


    private InjectionMetadata findInjectionMetadata(String beanName, Class<?> clazz, @Nullable PropertyValues pvs) {
        // Fall back to class name as cache key, for backwards compatibility with custom callers.
        String cacheKey = (StringUtils.hasLength(beanName) ? beanName : clazz.getName());
        // Quick check on the concurrent map first, with minimal locking.
        InjectionMetadata metadata = this.injectionMetadataCache.get(cacheKey);
        if (InjectionMetadata.needsRefresh(metadata, clazz)) {
            synchronized (this.injectionMetadataCache) {
                metadata = this.injectionMetadataCache.get(cacheKey);
                if (InjectionMetadata.needsRefresh(metadata, clazz)) {
                    if (metadata != null) {
                        metadata.clear(pvs);
                    }
                    metadata = buildInjectionMetadata(clazz);
                    this.injectionMetadataCache.put(cacheKey, metadata);
                }
            }
        }
        return metadata;
    }

    private InjectionMetadata buildInjectionMetadata(final Class<?> clazz) {
        List<InjectionMetadata.InjectedElement> elements = new ArrayList<>();
        Class<?> targetClass = clazz;

        do {
            final List<InjectionMetadata.InjectedElement> currElements = new ArrayList<>();

            ReflectionUtils.doWithLocalFields(targetClass, field -> {
                AnnotationAttributes ann = findAutowiredAnnotation(field);
                if (ann != null) {
                    if (Modifier.isStatic(field.getModifiers())) {
                        return;
                    }
                    currElements.add(new AnnotatedFieldElement(field, ann));
                }
            });

            elements.addAll(0, currElements);
            targetClass = targetClass.getSuperclass();
        }
        while (targetClass != null && targetClass != Object.class);

        return new InjectionMetadata(clazz, elements);
    }

    @Nullable
    private AnnotationAttributes findAutowiredAnnotation(AccessibleObject ao) {
        if (ao.getAnnotations().length > 0) {
            for (Class<? extends Annotation> type : this.annotationTypes) {
                AnnotationAttributes attributes = AnnotatedElementUtils.getMergedAnnotationAttributes(ao, type);
                if (attributes != null) {
                    return attributes;
                }
            }
        }
        return null;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = (ConfigurableListableBeanFactory) beanFactory;
    }

    /**
     * {@link Annotation Annotated} {@link Field} {@link InjectionMetadata.InjectedElement}
     */
    public class AnnotatedFieldElement extends InjectionMetadata.InjectedElement {

        private final Field field;

        private final AnnotationAttributes attributes;


        protected AnnotatedFieldElement(Field field, AnnotationAttributes attributes) {
            super(field, null);
            this.field = field;
            this.attributes = attributes;
        }

        @Override
        protected void inject(Object bean, String beanName, PropertyValues pvs) throws Throwable {

            Class<?> injectedType = field.getType();

            // 这里生产代理对象
            Object injectedObject = getInjectedObject(injectedType, this);

            ReflectionUtils.makeAccessible(field);

            field.set(bean, injectedObject);

        }

    }

    /**
     * Get injected-object from specified {@link AnnotationAttributes annotation attributes} and Bean Class
     *
     * @param injectedType    the type of injected-object
     * @param injectedElement {@link InjectionMetadata.InjectedElement}
     * @return An injected object
     */
    protected Object getInjectedObject(Class<?> injectedType,
                                       InjectionMetadata.InjectedElement injectedElement) {


        String cacheKey = buildInjectedObjectCacheKey(injectedType);

        Object injectedObject = injectedObjectsCache.get(cacheKey);

        if (injectedObject == null) {
            injectedObject = doGetInjectedBean(injectedType, injectedElement);
            // Customized inject-object if necessary
            injectedObjectsCache.putIfAbsent(cacheKey, injectedObject);
        }

        return injectedObject;

    }

    private Object doGetInjectedBean(Class<?> injectedType, InjectionMetadata.InjectedElement injectedElement) {


        String referenceBeanName = "@Reference" + injectedType.getName();

        RpcReferenceFactoryBean referenceBean = buildReferenceBeanIfAbsent(referenceBeanName, injectedType);

        registerReferenceBean(referenceBean, referenceBeanName);

        cacheInjectedReferenceBean(referenceBean, injectedElement);

        return getOrCreateProxy(referenceBean);
    }

    private Object getOrCreateProxy(RpcReferenceFactoryBean referenceBean) {

        try {
            return referenceBean.getObject();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

    }

    private void cacheInjectedReferenceBean(RpcReferenceFactoryBean referenceBean, InjectionMetadata.InjectedElement injectedElement) {
        if (injectedElement.getMember() instanceof Field) {
            injectedFieldReferenceBeanCache.put(injectedElement, referenceBean);
        }
    }

    private void registerReferenceBean(RpcReferenceFactoryBean referenceBean, String referenceBeanName) {
        ConfigurableListableBeanFactory beanFactory = getBeanFactory();
        if (!beanFactory.containsBean(referenceBeanName)) {
            beanFactory.registerSingleton(referenceBeanName, referenceBean);
        }
    }

    private ConfigurableListableBeanFactory getBeanFactory() {
        return beanFactory;
    }

    private RpcReferenceFactoryBean buildReferenceBeanIfAbsent(String referenceBeanName,
                                                               Class<?> referencedType) {

        RpcReferenceFactoryBean<?> referenceBean = referenceBeanCache.get(referenceBeanName);

        if (referenceBean == null) {
            referenceBean = new RpcReferenceFactoryBean<>(referencedType.getName(),beanFactory);
            referenceBeanCache.put(referenceBeanName, referenceBean);
        }
        return referenceBean;
    }

    private String buildInjectedObjectCacheKey(Class<?> injectedType) {
        return "RpcServiceBean" + injectedType.getName();
    }

}
