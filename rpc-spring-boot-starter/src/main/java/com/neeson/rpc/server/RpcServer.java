package com.neeson.rpc.server;

import cn.hutool.core.map.MapUtil;
import com.neeson.rpc.handler.codec.RpcDecoder;
import com.neeson.rpc.handler.codec.RpcEncoder;
import com.neeson.rpc.handler.RpcServerHandler;
import com.neeson.rpc.anno.RpcService;
import com.neeson.rpc.support.request.RpcRequest;
import com.neeson.rpc.support.response.RpcResponse;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.DependsOn;

import java.util.HashMap;
import java.util.Map;

/**
 * @author daile
 * @version 1.0
 * @date 2020/4/2 22:24
 */
@DependsOn(value = "serviceRegistry")
public class RpcServer implements ApplicationContextAware, InitializingBean {

    private Map<String, Object> handlerMap = new HashMap<>();

    private String host;
    private int port;
    private String serviceName;
    private ServiceRegistry serviceRegistry;

    public RpcServer(String serviceName,String host, int port, ServiceRegistry serviceRegistry) {
        this.serviceName = serviceName;
        this.host = host;
        this.port = port;
        this.serviceRegistry = serviceRegistry;
    }

    @Override
    public void afterPropertiesSet() {
        new Thread(()->{
            try {
                start();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void start() throws InterruptedException {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline()
                                    .addLast(new RpcDecoder(RpcRequest.class))
                                    .addLast(new RpcEncoder(RpcResponse.class))
                                    .addLast(new LoggingHandler(LogLevel.INFO))
                                    .addLast(new RpcServerHandler(handlerMap));
                        }
                    }).option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            ChannelFuture future = serverBootstrap.bind(host, port).sync();
            serviceRegistry.register(serviceName, host + ":" + port);
            future.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext ctx) throws BeansException {
        Map<String, Object> serviceBeanMap = ctx.getBeansWithAnnotation(RpcService.class);
        if (MapUtil.isNotEmpty(serviceBeanMap)) {
            for (Object serviceBean : serviceBeanMap.values()) {
                String interfaceName = serviceBean.getClass().getAnnotation(RpcService.class).value().getName();
                handlerMap.put(interfaceName, serviceBean);
            }
        }
    }
}
