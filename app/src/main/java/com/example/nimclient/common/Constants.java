package com.example.nimclient.common;

import com.example.proto.common.common.Common;

import java.util.Map;
import java.util.Random;

/**
 * @author kuro
 * @version V1.0
 * @date 20-2-26 下午8:31
 **/
public class Constants {

    /**
     * 用户id
     */
    public static final String UID = "1";

    /**
     * 心跳
     */
    public static final Common.Msg PING = Common.Msg.newBuilder()
            .setHead(Common.Head.newBuilder()
                    .setMsgType(Common.MsgType.HEART_BEAT)
                    .build())
            .build();

    /**
     * 握手
     */
    public static final Common.Msg HAND_SHAKE = Common.Msg.newBuilder()
            .setHead(Common.Head.newBuilder()
                    .setMsgType(Common.MsgType.HAND_SHAKE)
                    .addExtends(Common.ExtraHeader.newBuilder().setKey("uid").setValue(UID).build())
                    .build())
            .build();
    /**
     * chat服务的地址
     */
    public static final String CHAT_SERVICE = "http://192.168.0.108:8082";

    /**
     * 获取可用的netty地址
     */
    public static final String GET_AVAILABLE_NETTY_ADDRESS = CHAT_SERVICE + "/getAvailableNode";

    /**
     * 获取可用的netty地址
     */
    public static final String MSG_ACK = CHAT_SERVICE + "/ackMsg";

    /**
     * 获取可用的netty地址
     */
    public static final String UNREAD_MSG = CHAT_SERVICE + "/getMsg";

    public static String buildGetReq(String basePath, Map<String, String> params) {
        StringBuilder stringBuilder = new StringBuilder(basePath);
        stringBuilder.append("?");
        for (String key : params.keySet()) {
            stringBuilder.append(key).append("=").append(params.get(key)).append("&");
        }
        String s = stringBuilder.toString();
        return s.substring(0, s.length() - 1);
    }
}