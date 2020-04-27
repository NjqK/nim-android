package com.example.nimclient.netty.handler;

import android.util.Log;

import com.example.nimclient.common.Constants;
import com.example.nimclient.netty.TcpClient;
import com.example.nimclient.netty.policy.RetryPolicy;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.EventLoop;

import java.util.concurrent.TimeUnit;

/**
 * @author kuro
 * @version v1.0
 * @date 20-3-8 下午2:07
 **/
@ChannelHandler.Sharable
public class ReconnectHandler extends ChannelInboundHandlerAdapter {

    private static int retries = 0;
    private RetryPolicy retryPolicy;
    private TcpClient tcpClient;
    private final String tag = this.getClass().getName();

    public ReconnectHandler(TcpClient tcpClient) {
        this.tcpClient = tcpClient;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Log.i(tag, "====>netty active");
        retries = 0;
        // 发送确认连接的消息，携带用户id
        ctx.writeAndFlush(Constants.HAND_SHAKE);
        ctx.fireChannelActive();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (retries == 0) {
            Log.e(tag, "====>Lost the TCP connection with the server.");
            ctx.close();
        }
        boolean allowRetry = getRetryPolicy().allowRetry(retries);
        if (allowRetry) {
            ++retries;
            long sleepTimeMs = getRetryPolicy().getSleepIntervalMs(retries);
            Log.i(tag,"====>Try to reconnect to the server after " + sleepTimeMs + "ms. Retry count: " + retries);
            ctx.channel().eventLoop().schedule(() -> {
                Log.i(tag,"====>Reconnecting ...");
                tcpClient.connect();
            }, sleepTimeMs, TimeUnit.MILLISECONDS);
        } else {
            Log.e(tag,"====>over retry times ...");
            ctx.close();
            tcpClient.connectNewOne();
        }
        ctx.fireChannelInactive();
    }


    private RetryPolicy getRetryPolicy() {
        if (this.retryPolicy == null) {
            this.retryPolicy = tcpClient.getRetryPolicy();
        }
        return this.retryPolicy;
    }
}
