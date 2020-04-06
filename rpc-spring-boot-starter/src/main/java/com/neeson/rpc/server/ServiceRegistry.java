package com.neeson.rpc.server;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.Stat;
import org.springframework.util.Assert;

import java.util.Objects;

import static com.neeson.rpc.common.Constant.SERVICE_ROOT_PATH;

/**
 * @author daile
 * @version 1.0
 * @date 2020/4/2 22:12
 */
@Slf4j
public class ServiceRegistry {


    private CuratorFramework curatorFramework;

    public ServiceRegistry(CuratorFramework curatorFramework) {
        this.curatorFramework = curatorFramework;
    }

    /**
     * 注册服务  /service/serviceName/host+port
     *
     * @param serviceName
     * @param data
     */
    public void register(String serviceName, String data) {
        Assert.hasLength(data, "注册地址为空");
        try {
            Stat stat = curatorFramework.checkExists().forPath(SERVICE_ROOT_PATH + serviceName + "/" + data);
            if (Objects.isNull(stat)) {
                createNode(serviceName, data);
            } else {
                try {
                    curatorFramework.delete()
                            .forPath(SERVICE_ROOT_PATH + serviceName + "/" + data);
                } catch (KeeperException.NoNodeException e) {
                    createNode(serviceName, data);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private void createNode(String serviceName, String data) throws Exception {
        curatorFramework
                .create()
                .creatingParentsIfNeeded()
                .withMode(CreateMode.EPHEMERAL)
                .withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE)
                .forPath(SERVICE_ROOT_PATH + serviceName + "/" + data);
    }


}
