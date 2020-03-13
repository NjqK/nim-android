package com.example.nimclient;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.nimclient.service.impl.NettyService;

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
        Log.i(tag, "====>start service....");
        Intent intent = new Intent(this, NettyService.class);
        startService(intent);
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
