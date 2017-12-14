package com.yunfeng.tools.phoneproxy.socket;

import android.content.Context;

import com.yunfeng.tools.phoneproxy.util.Log;

import java.security.cert.CertificateException;

import javax.net.ssl.SSLException;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class Server {

    public static void main(String[] args) {
        new Server().startup("172.19.34.237", 8888);
    }

    public void startup(final String host, final int port) {
        System.setProperty("io.netty.noPreferDirect", "true");
        System.setProperty("io.netty.noUnsafe", "true");

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Server.this.run(host, port);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (SSLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (CertificateException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public boolean started = false;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private ServerInitializer serverInitializer = new ServerInitializer();

    private void run(String host, int port) throws InterruptedException,
            SSLException, CertificateException {
        started = true;
        try {
            bossGroup = new NioEventLoopGroup();
            workerGroup = new NioEventLoopGroup();
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(serverInitializer);
            b.option(ChannelOption.SO_KEEPALIVE, true);
            b.option(ChannelOption.SO_BACKLOG, 128);
            b.bind(host, port).sync().channel().closeFuture().sync();
        } catch (Exception e) {
            Log.e("start failed", e);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
        started = false;
    }

    public void stop() {
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
        started = false;
    }
}
