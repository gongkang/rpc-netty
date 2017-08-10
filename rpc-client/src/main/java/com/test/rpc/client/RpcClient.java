package com.test.rpc.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.test.rpc.common.RpcDecoder;
import com.test.rpc.common.RpcEncoder;
import com.test.rpc.common.RpcRequest;
import com.test.rpc.common.RpcResponse;

import java.util.concurrent.CountDownLatch;

/**
 * RPC 客户端（用于发送 RPC 请求）
 */
public class RpcClient extends SimpleChannelInboundHandler<RpcResponse> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcClient.class);

    private String host;
    private int port;

    private RpcResponse response;

    // private final Object obj = new Object();

    private CountDownLatch latch = new CountDownLatch(1);

    public RpcClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, RpcResponse response) throws Exception {
        this.response = response;

        /*synchronized (obj) {
            obj.notifyAll();
        }*/
        latch.countDown();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOGGER.error("client caught exception", cause);
        ctx.close();
    }

    public RpcResponse send(RpcRequest request) throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group).channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel channel) throws Exception {
                        channel.pipeline()
                            // TCP 消息粘包处理Handler（先写消息长度，再写消息）
                            .addLast(new LengthFieldBasedFrameDecoder(65536, 0, 4, 0, 0))
                            // 将 RPC 请求进行编码（为了发送请求）
                            .addLast(new RpcEncoder(RpcRequest.class))
                            // 将 RPC 请求响应解码（为了处理响应）
                            .addLast(new RpcDecoder(RpcResponse.class))
                            // 使用代理发送 RPC 请求
                            .addLast(RpcClient.this);
                    }
                })
                .option(ChannelOption.SO_KEEPALIVE, true);

            // 建立连接，发送请求
            ChannelFuture future = bootstrap.connect(host, port).sync();
            future.channel().writeAndFlush(request).sync();

            /*synchronized (obj) {
                obj.wait();
            }*/
            latch.await();

            if (response != null) {
                // 关闭通道
                future.channel().closeFuture().sync();
            }
            return response;
        } finally {
            group.shutdownGracefully();
        }
    }
}
