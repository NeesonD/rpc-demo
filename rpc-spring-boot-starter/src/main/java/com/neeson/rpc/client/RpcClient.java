package com.neeson.rpc.client;

import com.neeson.rpc.handler.codec.RpcDecoder;
import com.neeson.rpc.handler.codec.RpcEncoder;
import com.neeson.rpc.support.request.RpcRequest;
import com.neeson.rpc.support.response.RpcResponse;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * @author daile
 * @version 1.0
 * @date 2020/4/2 23:31
 */
@Slf4j
public class RpcClient extends SimpleChannelInboundHandler<RpcResponse> {
    private String host;
    private int port;

    private RpcResponse response;

    public RpcClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, RpcResponse response) {
        this.response = response;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("client caught exception", cause);
        ctx.close();
    }

    public RpcResponse send(RpcRequest request,RpcClient rpcClient) throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group).channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel channel) throws Exception {
                            channel.pipeline()
                                    .addLast(new RpcEncoder(RpcRequest.class)) // 将 RPC 请求进行编码（为了发送请求）
                                    .addLast(new RpcDecoder(RpcResponse.class)) // 将 RPC 响应进行解码（为了处理响应）
                                    .addLast(new LoggingHandler(LogLevel.INFO))
                                    .addLast(rpcClient); // 使用 RpcClient 发送 RPC 请求
                        }
                    })
                    .option(ChannelOption.TCP_NODELAY, true);

            ChannelFuture future = bootstrap.connect(host, port).sync();
            future.channel().writeAndFlush(request).sync();
            future.channel().closeFuture().sync();
            return response;
        } finally {
            group.shutdownGracefully();
        }
    }
}
