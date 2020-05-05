package com.example.nimclient.common;

import com.example.nimclient.netty.secure.aes.AESUtil;

public class KeyManager {
    /**
     * 客户端AES 密钥
     */
    public static String CLIENT_AES_KEY;
    /**
     * 服务端RSA 公钥
     */
    public static String SERVER_RSA_PUBLIC_KEY;

    public KeyManager(String serverRsaPublicKey) {
        CLIENT_AES_KEY = AESUtil.createKeys();
        SERVER_RSA_PUBLIC_KEY = serverRsaPublicKey;
    }
}
