package io.netty.demo;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetSocketAddress;

public class EchoServer {

    private final int port;

    EchoServer(int port) {
        this.port = port;
    }

    public static void main(String[] args) throws Exception {
        new EchoServer(8089).start();
        System.out.println("server bind success!");
    }

    public void start() throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            ServerBootstrap s = new ServerBootstrap();
            s.group(group)
                    .channel(NioServerSocketChannel.class)
                    .localAddress(new InetSocketAddress(port))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast(new EchoServerHandler());
                        }
                    });
            ChannelFuture f = s.bind().sync();
            System.out.println(EchoServer.class.getName()+"start listen on "+f.channel().localAddress());
            f.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully().sync();
        }
    }


}

