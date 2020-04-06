package com.neeson.rpc;

import com.neeson.rpc.client.RpcProxy;
import com.neeson.rpc.client.ServiceDiscovery;
import com.neeson.rpc.server.RpcServer;
import com.neeson.rpc.server.ServiceRegistry;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author daile
 * @version 1.0
 * @date 2020/4/2 22:04
 */
@Configuration
@ComponentScan
@EnableConfigurationProperties(RpcProperties.class)
public class RpcAutoConfiguration {

    @ConditionalOnMissingBean
    @Bean(initMethod = "start", destroyMethod = "close")
    public CuratorFramework curatorFramework(RpcProperties properties) {
        return CuratorFrameworkFactory.builder()
                .connectString(properties.getCuratorProperties().getConnectString())
                .sessionTimeoutMs(properties.getCuratorProperties().getSessionTimeoutMs())
                .connectionTimeoutMs(properties.getCuratorProperties().getConnectionTimeoutMs())
                .retryPolicy(new RetryNTimes(properties.getCuratorProperties().getRetryCount(), properties.getCuratorProperties().getElapsedTimeMs()))
                .build();
    }

    @Configuration
    @ConditionalOnProperty(prefix = "rpc.server", name = "enable", havingValue = "true")
    protected static class ServerConfiguration {

        @Bean
        public ServiceRegistry serviceRegistry(CuratorFramework curatorFramework) {
            return new ServiceRegistry(curatorFramework);
        }

        @Bean
        public RpcServer rpcServer(RpcProperties rpcProperties, ServiceRegistry serviceRegistry) {
            return new RpcServer(
                    rpcProperties.getServer().getServiceName(),
                    rpcProperties.getServer().getHost(),
                    rpcProperties.getServer().getPort(),
                    serviceRegistry);
        }
    }

    @Configuration
    @ConditionalOnProperty(prefix = "rpc.client", name = "enable", havingValue = "true")
    protected static class ClientConfiguration {

        @Bean
        public ServiceDiscovery serviceDiscovery(RpcProperties rpcProperties,CuratorFramework curatorFramework) {
            String serviceNames = rpcProperties.getClient().getServiceNames();
            String[] serviceNameList = serviceNames.split(",");
            return new ServiceDiscovery(curatorFramework,serviceNameList);
        }

        @Bean
        public RpcProxy rpcProxy(ServiceDiscovery serviceDiscovery) {
            return new RpcProxy(serviceDiscovery);
        }
    }





}
