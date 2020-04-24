package com.example.nimclient.service.impl;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.nimclient.common.Constants;
import com.example.nimclient.common.MsgSenderMap;
import com.example.nimclient.common.OkHttpUtil;
import com.example.nimclient.netty.TcpClient;
import com.example.nimclient.service.FailedReconnect;
import com.example.nimclient.service.MsgSender;
import com.example.proto.common.common.Common;
import com.example.proto.outer.outer.Outer;
import com.google.protobuf.util.JsonFormat;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Response;

public class NettyService extends Service implements OkHttpUtil.NetCall, MsgSender, FailedReconnect {

    /**
     * log tag
     */
    private final String tag = this.getClass().getName();

    /**
     * Netty client
     */
    private TcpClient tcpClient = null;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.i(tag, "====>service on create");
//        cmdReceiver = new CommandReceiver();
        OkHttpUtil instance = OkHttpUtil.getInstance();
        instance.getDataAsyn(Constants.GET_AVAILABLE_NETTY_ADDRESS, this);
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        IntentFilter filter = new IntentFilter();
        filter.addAction("NettyService");
        //registerReceiver(cmdReceiver, filter);
        MsgSenderMap.addSender(tag, this);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        //this.unregisterReceiver(cmdReceiver);
        // close netty service
        if (tcpClient != null) {
            tcpClient.close();
        }
        MsgSenderMap.remove(tag);
        super.onDestroy();
    }

    @Override
    public void success(Call call, Response response) throws IOException {
        Log.e(tag, "====>invoke successfully.");
        byte[] bytes = response.body().bytes();
        Outer.GetAvailableNodeResp resp = Outer.GetAvailableNodeResp.parseFrom(bytes);
        if (resp.getRet().getErrorCode().equals(Common.ErrCode.SUCCESS)) {
            // 请求成功
            String json = JsonFormat.printer().print(resp);
            Log.i(tag, "====>data: " + json);
            // launch netty client
            tcpClient = new TcpClient(resp.getHost(),
                    Integer.parseInt(resp.getPort()),
                    getApplication(), this);
            tcpClient.connect();
        } else {
            Log.e(tag, "====>chat service is unavailable.");
            onDestroy();
        }
    }

    @Override
    public void failed(Call call, IOException e) {
        Log.e(tag, "====>chat service is unavailable.");
        sendMsg("chat service is unavailable.");
        onDestroy();
    }

    @Override
    public void sendMsg(String msg) {
        Intent intent = new Intent();
        intent.setAction("MainActivity");
        intent.putExtra("data", msg);
        sendBroadcast(intent);
    }

    @Override
    public void searchNewAvailableNodeAndConnect() {
        OkHttpUtil instance = OkHttpUtil.getInstance();
        instance.getDataAsyn(Constants.GET_AVAILABLE_NETTY_ADDRESS, this);
    }
}
