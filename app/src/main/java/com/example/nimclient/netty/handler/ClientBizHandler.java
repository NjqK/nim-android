package com.example.nimclient.netty.handler;

import android.util.Log;

import com.example.nimclient.common.Constants;
import com.example.nimclient.common.MsgSenderMap;
import com.example.nimclient.common.OkHttpUtil;
import com.example.nimclient.service.MsgSender;
import com.example.nimclient.service.impl.NettyService;
import com.example.proto.common.common.Common;

import java.util.HashMap;
import java.util.Map;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * @author kuro
 * @version v1.0
 * @date 20-3-7 下午6:01
 **/
public class ClientBizHandler extends ChannelInboundHandlerAdapter {

//    @Override
//    public void channelActive(ChannelHandlerContext ctx) throws Exception {
//        ctx.writeAndFlush(defaultMsg());
//    }
//
//    private Common.Msg defaultMsg() {
//        Common.Msg.Builder builder = Common.Msg.newBuilder();
//        Common.Head header = Common.Head.newBuilder()
//                .setMsgType(Common.MsgType.SINGLE_CHAT)
//                .setMsgContentType(Common.MsgContentType.TEXT)
//                .build();
//        Common.Body body = Common.Body.newBuilder()
//                .setContent("msgBody received")
//                .build();
//        builder.setHead(header);
//        builder.setBody(body);
//        return builder.build();
//    }

    private final String tag = this.getClass().getName();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Common.Msg message = (Common.Msg) msg;
        Common.MsgType msgType = message.getHead().getMsgType();
        switch (msgType) {
            case SINGLE_CHAT:
            case MULTI_CHAT:
                Log.i(tag, "====>Client received msg: " + message);
                MsgSender sender = MsgSenderMap.getSender(NettyService.class.getName());
                if (sender != null) {
                    sender.sendMsg(message.getBody().getContent());
                }
                // 确认消息收到
                Map<String, String> param = new HashMap<>(4);
                param.put("uid", Constants.UID);
                param.put("guid", String.valueOf(message.getHead().getMsgId()));
                String reqPath = Constants.buildReqPath(Constants.MSG_ACK, param);
                Log.i(tag, "====>" + reqPath);
                OkHttpUtil.getInstance().getDataAsyn(reqPath, OkHttpUtil.DEFAULT_NET_CALL);
                Log.i(tag, "====>send ack...");
                break;
            case KICK:
                Log.i(tag, "====>Be kicked by server: " + message);
                break;
            case BYE:
                Log.i(tag, "====>Server down: " + message);
                break;
            default:
                break;
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // TODO 拉去用户未读消息
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
