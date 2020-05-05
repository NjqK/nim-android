package com.example.nimclient.common;

import android.app.Application;

import com.example.common.secure.aes.AESUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class KeyManager {
    /**
     * 客户端AES 密钥
     */
    public static final String CLIENT_AES_KEY;
    /**
     * 服务端RSA 公钥
     */
    public static final String SERVER_RSA_PUBLIC_KEY;

    static {
        CLIENT_AES_KEY = AESUtil.createKeys();
        SERVER_RSA_PUBLIC_KEY = getServerPublicKey();
    }

    private static String getServerPublicKey() {
        try (FileReader reader = new FileReader(
                new File("app/src/main/assets/key/rsa/public_key.txt"));
             BufferedReader bReader = new BufferedReader(reader);) {
            StringBuilder sb = new StringBuilder();//定义一个字符串缓存，将字符串存放缓存中
            String s = "";
            while ((s = bReader.readLine()) != null) {//逐行读取文件内容，不读取换行符和末尾的空格
                sb.append(s + "\n");//将读取的字符串添加换行符后累加存放在缓存中
            }
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("读取服务器密钥失败");
        }
    }
}
