package com.neeson.rpc.client;

import com.neeson.rpc.common.Constant;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.Watcher;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author daile
 * @version 1.0
 * @date 2020/4/2 23:25
 */
@Slf4j
public class ServiceDiscovery {

    private volatile ConcurrentHashMap<String, List<String>> serviceCache = new ConcurrentHashMap<>();

    private CuratorFramework curatorFramework;
    private String[] serviceNameList;

    public ServiceDiscovery(CuratorFramework curatorFramework, String[] serviceNameList) {
        this.curatorFramework = curatorFramework;
        this.serviceNameList = serviceNameList;
    }

    /**
     * /service/serviceName/host+port
     */
    @PostConstruct
    private void init() {
        for (String serviceName : serviceNameList) {
            resetServiceCache(serviceName);
        }
    }
    private void resetServiceCache(String serviceName) {
        log.info("resetServiceCache==>" +serviceName);
        List<String> nodeList;
        try {
            nodeList = curatorFramework.getChildren()
                    .usingWatcher((Watcher) watchedEvent -> resetServiceCache(serviceName))
                    .forPath(Constant.SERVICE_ROOT_PATH + serviceName);
            serviceCache.put(serviceName, nodeList);
        } catch (Exception e) {
            log.error(e.getMessage(),e);
        }

    }

    /**
     * 后续做 loadBalance
     * @param serviceName
     * @return
     */
    @SneakyThrows
    public String discover(String serviceName) {
        List<String> dataList = serviceCache.get(serviceName);
        String data = null;
        int size = dataList.size();
        if (size > 0) {
            if (size == 1) {
                data = dataList.get(0);
                log.debug("using only data: {}", data);
            } else {
                data = dataList.get(ThreadLocalRandom.current().nextInt(size));
                log.debug("using random data: {}", data);
            }
        }
        return data;
    }

}
