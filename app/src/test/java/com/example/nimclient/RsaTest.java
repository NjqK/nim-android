package com.example.nimclient;

import com.example.nimclient.common.KeyManager;
import com.example.nimclient.netty.secure.aes.AESUtil;
import com.example.nimclient.netty.secure.rsa.RSAUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.security.interfaces.RSAPublicKey;

public class RsaTest {
    public static void main(String[] args) throws Exception {
        liucheng();
    }

    private static void liucheng() throws Exception {
        File file = new File("app/src/main/assets/key/rsa/public_key.txt");
        FileReader reader = new FileReader(file);//定义一个fileReader对象，用来初始化BufferedReader
        BufferedReader bReader = new BufferedReader(reader);//new一个BufferedReader对象，将文件内容读取到缓存
        StringBuilder sb = new StringBuilder();//定义一个字符串缓存，将字符串存放缓存中
        String s = "";
        while ((s =bReader.readLine()) != null) {//逐行读取文件内容，不读取换行符和末尾的空格
            sb.append(s + "\n");//将读取的字符串添加换行符后累加存放在缓存中
        }
        bReader.close();
        String publicKey = sb.toString();
        System.out.println(publicKey);
        RSAPublicKey rsaPublicKey = RSAUtils.getPublicKey(publicKey);
        System.out.println(rsaPublicKey);

        // 客户端使用服务端公钥加密传输AES密钥
        String clientAESKey = AESUtil.createKeys();
        System.out.println("client: "+clientAESKey);
        String msgAfterCode = RSAUtils.publicEncrypt(clientAESKey, rsaPublicKey);
        System.out.println("利用服务器公钥加密后"+msgAfterCode);
        // 服务端收到客户端密钥后用客户端密钥发送自己的AES密钥，每个用户对应不用的AES密钥
        String serverPrivateKey = "MIICeAIBADANBgkqhkiG9w0BAQEFAASCAmIwggJeAgEAAoGBAPbiGJHfCMqloCFT2dLLu62jeFMadFLgYi3el6tEM5aR_jk0Andk9HQ__28lTUmPzSL0wECww_ry2mL1AoKkhIbqGsr-0IZwpamk2M3fDGxeNlG0KLrVFwyj44NeggycdQs1wkOk6Pdh3YuvHNMdAotOzuqvKh7QNjfmXaaeic4TAgMBAAECgYEAiTwZSEzgiDUVFDGWLbUOeHEcG6Xi74bHTJQlXxCkVJiG5qlgjZnSwSQqC1CC69dBwqKmk88uwbppZwSnBpQJDhZozab52Na38eevJfbYNsDxJ6n9pdQhYjpM3ZWA0XCGD6O8xUpKqQzi6iXmrUbPutK7ja8jvEcxWh8Zqmh_slECQQD8ROBqfX3mY9SJ3siQIfkVqF4E-FUU6IOuvX1UlELPRMiKs0wfuYfgtwkiPU2WFhK6pJXwhv4Us7as0FDfj2YXAkEA-ojT6UlStwdgQpalF5xWzRoKmVNIGPuSRyJJM7IQkd5dVKnaR50QansjU4olrhJyzJdHBdOa_BeVh7lQgKkRZQJACSNdlb3x_5SCMHRXg5EXesdckIWGX3mEu6G1lojAWs29DfksusF3wJYgyJK76sHl78jifZIGRi20YlIxe8ewBQJBAOTyeUUs_mvIT81KQWBMPH5-F8V5997stwZObLrTNJU4se2WsqTTAZdtJCFJk5l5vnL8o6jNcUqCeuFnFgwddIkCQQCUzKHsishIEu4I1Q1ue7zzeLNQMCXh1JDHN1iU07kRivM55I6si3c4_K3Hy5saLm1A4ABOlRBgG-FU1U6JDLgM";
        String afterDecrypt = RSAUtils.privateDecrypt(msgAfterCode, RSAUtils.getPrivateKey(serverPrivateKey));
        System.out.println("利用服务器密私钥解密后:"+afterDecrypt);
        // 保存密钥，并生成自己的
        String serverAESKey = AESUtil.createKeys();
        System.out.println("server AES:"+serverAESKey);
        // 用户客户端AES加密
        String serverAESKeyAfterEncrypted = AESUtil.aesEncrypt(serverAESKey, clientAESKey);
        // 客户端解密并保存
        String serverAESKeyBase64 = AESUtil.aesDecrypt(serverAESKeyAfterEncrypted, clientAESKey);
        System.out.println(serverAESKeyBase64);

        // 之后服务端使用客户端AES加密 客户端使用服务端AES加密
    }
}
