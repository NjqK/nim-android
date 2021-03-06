package com.example.nimclient.netty.handler;

import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.example.nimclient.common.Constants;
import com.example.nimclient.common.KeyManager;
import com.example.nimclient.common.MsgSenderMap;
import com.example.nimclient.common.OkHttpUtil;
import com.example.nimclient.netty.TcpClient;
import com.example.nimclient.netty.secure.aes.AESUtil;
import com.example.nimclient.service.MsgSender;
import com.example.nimclient.service.impl.NettyService;
import com.example.proto.common.common.Common;
import com.example.proto.outer.outer.Outer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import okhttp3.Call;
import okhttp3.Response;

/**
 * @author kuro
 * @version v1.0
 * @date 20-3-7 下午6:01
 **/
public class ClientBizHandler extends ChannelInboundHandlerAdapter {

    private final String tag = this.getClass().getName();

    private TcpClient tcpClient;

    public ClientBizHandler(TcpClient tcpClient) {
        this.tcpClient = tcpClient;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Common.Msg message = (Common.Msg) msg;
        Common.MsgType msgType = message.getHead().getMsgType();
        switch (msgType) {
            case SINGLE_CHAT:
            case MULTI_CHAT:
                // TODO 目前支队这两个类型进行消息加解密
                Log.i(tag, "====>Client received msg: " + message);
                MsgSender sender = MsgSenderMap.getSender(NettyService.class.getName());
                if (sender != null) {
                    String content = message.getBody().getContent();
                    String afterDecrypt = AESUtil.aesDecrypt(content, KeyManager.CLIENT_AES_KEY);
                    sender.sendMsg(afterDecrypt);
                }
                // 确认消息收到
                Map<String, String> param = new HashMap<>(4);
                param.put("uid", Constants.UID);
                param.put("guid", String.valueOf(message.getHead().getMsgId()));
                String reqPath = Constants.buildGetReq(Constants.MSG_ACK, param);
                Log.i(tag, "====>" + reqPath);
                OkHttpUtil.getInstance().getDataAsyn(reqPath, OkHttpUtil.DEFAULT_NET_CALL);
                Log.i(tag, "====>send ack...");
                break;
            case KICK:
                Log.i(tag, "====>Be kicked by server: " + message);
                tcpClient.shutDown();
                break;
            case BYE:
                Log.i(tag, "====>Server down: " + message);
                break;
            case CHANGE_SERVER:
                Log.i(tag, "====>Manually Change Server: " + message);
                ctx.close();
                tcpClient.connectSpecialOne(message.getBody().getContent());
                break;
            default:
                break;
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // 拉去用户未读消息
        Map<String, String> params = new HashMap<>(4);
        params.put("uid", Constants.UID);
        // 本地不保存maxGuid的话就传0
        params.put("mGuid", "0");
        String unreadMsgReq = Constants.buildGetReq(Constants.UNREAD_MSG, params);
        OkHttpUtil.getInstance().getDataAsyn(unreadMsgReq, new OkHttpUtil.NetCall() {
            @Override
            public void success(Call call, Response response) throws IOException {
                byte[] body = response.body().bytes();
                Outer.GetUnreadMsgResp resp = Outer.GetUnreadMsgResp.parseFrom(body);
                if (resp.getRet().getErrorCode().equals(Common.ErrCode.FAIL)) {
                    Log.e(tag, "====>接口响应失败.");
                    return;
                }
                List<Common.Msg> msgsList = resp.getMsgsList();
                if (msgsList != null && msgsList.size() > 0) {
                    List<String> msgs = new ArrayList<>(msgsList.size());
                    for (Common.Msg msg : msgsList) {
                        msgs.add(msg.getBody().getContent());
                    }
                    MsgSender sender = MsgSenderMap.getSender(NettyService.class.getName());
                    String allMsgJson = JSON.toJSONString(msgs, true);
                    sender.sendMsg(allMsgJson);
                }
            }

            @Override
            public void failed(Call call, IOException e) {
                Log.e(tag, "====>网络请求失败.");
            }
        });

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
