package com.neeson.rpc;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author daile
 * @version 1.0
 * @date 2020/4/2 22:07
 */
@ConfigurationProperties(prefix = "rpc")
public class RpcProperties {

    private final Client client = new Client();

    private final Server server = new Server();

    private final CuratorProperties curatorProperties = new CuratorProperties();

    public Client getClient() {
        return client;
    }

    public Server getServer() {
        return server;
    }

    public CuratorProperties getCuratorProperties() {
        return curatorProperties;
    }

    public static class Client {
        private String serviceNames;
        private boolean enable;

        public boolean isEnable() {
            return enable;
        }

        public void setEnable(boolean enable) {
            this.enable = enable;
        }
        public String getServiceNames() {
            return serviceNames;
        }

        public void setServiceNames(String serviceNames) {
            this.serviceNames = serviceNames;
        }
    }

    public static class Server {

        private boolean enable;

        public boolean isEnable() {
            return enable;
        }

        public void setEnable(boolean enable) {
            this.enable = enable;
        }

        private String host;
        private int port;
        private String serviceName;


        public String getServiceName() {
            return serviceName;
        }

        public void setServiceName(String serviceName) {
            this.serviceName = serviceName;
        }

        public Integer getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }
    }

    public static class CuratorProperties {

        private int retryCount;

        private int elapsedTimeMs;

        private String connectString;

        private int sessionTimeoutMs;

        private int connectionTimeoutMs;

        public int getRetryCount() {
            return retryCount;
        }

        public void setRetryCount(int retryCount) {
            this.retryCount = retryCount;
        }

        public int getElapsedTimeMs() {
            return elapsedTimeMs;
        }

        public void setElapsedTimeMs(int elapsedTimeMs) {
            this.elapsedTimeMs = elapsedTimeMs;
        }

        public String getConnectString() {
            return connectString;
        }

        public void setConnectString(String connectString) {
            this.connectString = connectString;
        }

        public int getSessionTimeoutMs() {
            return sessionTimeoutMs;
        }

        public void setSessionTimeoutMs(int sessionTimeoutMs) {
            this.sessionTimeoutMs = sessionTimeoutMs;
        }

        public int getConnectionTimeoutMs() {
            return connectionTimeoutMs;
        }

        public void setConnectionTimeoutMs(int connectionTimeoutMs) {
            this.connectionTimeoutMs = connectionTimeoutMs;
        }
    }


}
