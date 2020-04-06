#### Rpc 框架

**參考**

* https://gitee.com/huangyong/rpc/tree/master

![](/doc/pic/rpc.png)

[设计](https://mubu.com/doc/6US5AgtAuT_)

* oss 
    * oss-api
    * oss-client (客户端)
    * oss-svc (服务端)
* rpc-spring-boot-starter（基础组件）

#### 启动

本地安装一个 zookeeper 

```
windows 闪退看这个：https://blog.csdn.net/qq279862451/article/details/79083522
```

* run oss-svc (这个可以开多个)
* run oss-client
* 测试 oss-client 中的接口即可

```
http://localhost:10003/OssClientController/getOssToken
```