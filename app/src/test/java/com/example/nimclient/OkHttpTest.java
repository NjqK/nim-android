package com.example.nimclient;

import com.alibaba.fastjson.JSON;
import com.example.nimclient.common.Constants;
import com.example.nimclient.common.OkHttpUtil;
import com.example.nimclient.entity.NettyNode;
import com.example.proto.outer.outer.Outer;
import com.google.protobuf.util.JsonFormat;

import java.io.IOException;

import okhttp3.Response;

public class OkHttpTest {

    public static void main(String[] args) throws IOException {
        OkHttpUtil instance = OkHttpUtil.getInstance();
        Response data = instance.getData(Constants.GET_AVAILABLE_NETTY_ADDRESS);
        byte[] bytes = data.body().bytes();
        Outer.GetAvailableNodeResp resp = Outer.GetAvailableNodeResp.parseFrom(bytes);
        System.out.println(JsonFormat.printer().print(resp));
    }
}
