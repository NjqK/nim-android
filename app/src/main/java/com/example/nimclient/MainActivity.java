package com.example.nimclient;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.nimclient.common.KeyManager;
import com.example.nimclient.service.impl.NettyService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity {

    private final String tag = this.getClass().getName();

    private TextView tv;

    private DataReceiver dataReceiver;

    private class DataReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String data = intent.getStringExtra("data");
            tv.setText(data);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv = findViewById(R.id.printInfo);
        initKeyManager();
        Log.i(tag, "====>start service....");
        Intent intent = new Intent(this, NettyService.class);
        startService(intent);
    }

    private void initKeyManager() {
        try {
            InputStream open = getAssets().open("key/rsa/public_key.txt");
            InputStreamReader inputStreamReader = new InputStreamReader(open);
            BufferedReader bReader = new BufferedReader(inputStreamReader);
            StringBuilder sb = new StringBuilder();//定义一个字符串缓存，将字符串存放缓存中
            String s = "";
            while ((s = bReader.readLine()) != null) {//逐行读取文件内容，不读取换行符和末尾的空格
                sb.append(s + "\n");//将读取的字符串添加换行符后累加存放在缓存中
            }
            String serverPublicKey = sb.toString();
            new KeyManager(serverPublicKey);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStart() {//重写onStart方法
        dataReceiver = new DataReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("MainActivity");
        registerReceiver(dataReceiver, filter);
        super.onStart();
    }
    @Override
    protected void onStop() {//重写onStop方法
        unregisterReceiver(dataReceiver);
        super.onStop();
    }
}
