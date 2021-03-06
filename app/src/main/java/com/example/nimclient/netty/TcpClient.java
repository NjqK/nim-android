package com.example.nimclient.netty;

import android.content.Context;
import android.util.Log;

import com.example.nimclient.netty.handler.ClientBizHandler;
import com.example.nimclient.netty.handler.PingerHandler;
import com.example.nimclient.netty.handler.ReconnectHandler;
import com.example.nimclient.netty.policy.DefaultRetryPolicy;
import com.example.nimclient.netty.policy.RetryPolicy;
import com.example.nimclient.netty.ssl.SSLContextFactory;
import com.example.nimclient.service.SpecialReaction;
import com.example.proto.common.common.Common;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.ssl.SslHandler;

/**
 * @author kuro
 * @version v1.0
 * @date 20-3-8 下午1:56
 **/
public class TcpClient {

    private final String tag = this.getClass().getName();

    private String host;
    private int port;
    private Bootstrap bootstrap;
    private EventLoopGroup group = new NioEventLoopGroup();
    /**
     * 重连策略
     */
    private RetryPolicy retryPolicy;
    /**
     * 可用于在其他非handler的地方发送数据
     */
    private Channel channel;
    private TcpClient client;
    private Context context;
    private SpecialReaction specialReaction;

    public TcpClient(String host, int port, Context context, SpecialReaction specialReaction) {
        this(host, port, DefaultRetryPolicy.DEFAULT);
        this.specialReaction = specialReaction;
        this.context = context;
    }

    public TcpClient(String host, int port, RetryPolicy retryPolicy) {
        this.host = host;
        this.port = port;
        this.retryPolicy = retryPolicy;
        client = this;
        init();
    }

    /**
     * 向远程TCP服务器请求连接
     */
    public void connect() {
        Log.i(tag, "====>connecting netty server.");
        ChannelFuture future = bootstrap.connect(host, port);
        future.addListener(getConnectionListener());
        this.channel = future.channel();
    }

    public void close() {
        Log.i(tag, "====>closing netty server.");
        try {
            channel.closeFuture().syncUninterruptibly();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }
    }

    public void shutDown() {
        close();
        specialReaction.shutDownApp();
    }

    /**
     * 重新连接一个新的
     */
    public void connectNewOne() {
        close();
        specialReaction.searchNewAvailableNodeAndConnect();
    }

    /**
     * 连接特定的一个服务器
     */
    public void connectSpecialOne(String newServerInfo) {
        if (newServerInfo != null && newServerInfo.trim().length() > 0) {
            close();
            String[] server = newServerInfo.split("_");
            String host = server[1];
            String port = server[2];
            specialReaction.connectSpecialServer(host, port);
        }
    }

    public RetryPolicy getRetryPolicy() {
        return retryPolicy;
    }

    private void init() {
        // bootstrap 可重用, 只需在TcpClient实例化的时候初始化即可.
        bootstrap = new Bootstrap();
        bootstrap.group(group).channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(getChannelInitializer());
    }

    private ChannelInitializer<Channel> getChannelInitializer() {
        return new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel ch) throws Exception {
                ChannelPipeline pipeline = ch.pipeline();
                // ssl
                //SSLContext sslContext = SSLContextFactory.getClientContext(context);
                //SSLEngine sslEngine = sslContext.createSSLEngine();
                //sslEngine.setUseClientMode(true);
                //ch.pipeline().addLast("ssl", new SslHandler(sslEngine));
                // 半包处理
                pipeline.addLast(new ProtobufVarint32FrameDecoder());
                // 解码的目标类
                pipeline.addLast(new ProtobufDecoder(Common.Msg.getDefaultInstance()));
                pipeline.addLast(new ProtobufVarint32LengthFieldPrepender());
                pipeline.addLast(new ProtobufEncoder());
                pipeline.addLast(new ReconnectHandler(client));
                pipeline.addLast(new PingerHandler());
                pipeline.addLast(new ClientBizHandler(client));
            }
        };
    }

    private ChannelFutureListener getConnectionListener() {
        return future -> {
            if (!future.isSuccess()) {
                Log.i(tag, "====>connecting is failed");
                future.channel().pipeline().fireChannelInactive();
            }
        };
    }
}

