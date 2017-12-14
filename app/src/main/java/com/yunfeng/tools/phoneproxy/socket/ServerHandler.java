package com.yunfeng.tools.phoneproxy.socket;

import com.yunfeng.tools.phoneproxy.Log;
import com.yunfeng.tools.phoneproxy.MyX509TrustManager;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.PemX509Certificate;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;

import java.io.File;
import java.io.IOException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLEngine;

@Sharable
public class ServerHandler extends ChannelInboundHandlerAdapter {

    private final class ServerToServerHandler extends
            ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg)
                throws Exception {
            ctx.channel().writeAndFlush(msg);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
                throws Exception {
            if (cause instanceof IOException) {
                Log.d("exceptionCaught" + cause.getMessage());
            } else {
                Log.e("exceptionCaught", cause);
            }
            cause.printStackTrace();
        }
    }

    private ChannelFuture cf;
    private String host;
    private int port;

    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg)
            throws Exception {
        if (msg instanceof FullHttpRequest) {
            final FullHttpRequest request = (FullHttpRequest) msg;
            String hostdata = request.headers().get(HttpHeaderNames.HOST);
            if (null != hostdata) {
                String[] hostarr = hostdata.split(":");
                int port = 80;
                if (hostarr.length == 2) {
                    port = Integer.valueOf(hostarr[1]);
                }
                String host = hostarr[0];

                this.host = host;
                this.port = port;
                if ("CONNECT".equalsIgnoreCase(request.method().name())) {
                    HttpResponse response = new DefaultFullHttpResponse(
                            HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
                    ctx.writeAndFlush(response);
                    ctx.pipeline().remove("httpCodec");
                    ctx.pipeline().remove("httpObject");
                    return;
                }
                Bootstrap b = new Bootstrap();
                b.group(ctx.channel().eventLoop())
                        .channel(ctx.channel().getClass())
                        .handler(new HttpProxyInitializer(ctx.channel()));
                // Make the connection attempt.
                cf = b.connect(host, port);
                cf.addListener(new ChannelFutureListener() {

                    @Override
                    public void operationComplete(ChannelFuture future)
                            throws Exception {
                        // TODO Auto-generated method stub
                        if (future.isSuccess()) {
                            future.channel().writeAndFlush(msg);
                        } else {
                            ctx.channel().close();
                        }
                    }
                });
            }
        } else if (msg instanceof HttpContent) {

        } else if (msg instanceof ByteBuf) {
            ByteBuf bytebuf = (ByteBuf) msg;
            if (bytebuf.getByte(0) == 22) {
//                final X509Certificate cert = Cert.getCert(this.host);
//                SslContext sslContext = SslContextBuilder.forServer(
//                        Cert.serverPriKey, cert).sslContextProvider(Cert.provider).build();
//                final X509Certificate[] trustCertCollection = {cert};
//                SslContext sslContext = new BCSslContext(Cert.provider, null, null,
//                        trustCertCollection, Cert.serverPriKey, null, null, 0, 0);
                // Configure SSL.
                final SslContext sslCtx = SslContextBuilder.forClient()
                        .trustManager(InsecureTrustManagerFactory.INSTANCE).build();

                ctx.pipeline().addFirst(new HttpServerCodec());
                ctx.pipeline().addFirst(sslCtx.newHandler(ctx.alloc()));
                //重新过一遍pipeline，拿到解密后的的http报文
                ctx.pipeline().fireChannelRead(msg);
            }
        } else {
            if (cf == null) {
                Bootstrap b = new Bootstrap();
                b.group(ctx.channel().eventLoop())
                        .channel(ctx.channel().getClass())
                        .handler(new ChannelInitializer<Channel>() {

                            @Override
                            protected void initChannel(Channel ch)
                                    throws Exception {
                                ChannelPipeline p = ch.pipeline();
                                p.addLast("logging", new LoggingHandler(
                                        LogLevel.DEBUG));
                                p.addLast(new ServerToServerHandler());
                            }
                        });
                // Make the connection attempt.
                ChannelFuture cf = b.connect(host, port);
                cf.addListener(new ChannelFutureListener() {

                    @Override
                    public void operationComplete(ChannelFuture future)
                            throws Exception {
                        // TODO Auto-generated method stub
                        if (future.isSuccess()) {
                            future.channel().writeAndFlush(msg);
                        } else {
                            ctx.channel().close();
                        }
                    }
                });
            } else {
                cf.channel().writeAndFlush(msg);
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        if (cause instanceof IOException) {
            Log.d("exceptionCaught" + cause.getMessage());
        } else {
            Log.e("exceptionCaught", cause);
        }
        cause.printStackTrace();
    }

}
