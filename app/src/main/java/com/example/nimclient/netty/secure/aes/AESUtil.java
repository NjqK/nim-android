package com.example.nimclient.netty.secure.aes;


import org.apaches.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * @author kuro
 * @version V1.0
 * @date 2020-05-05 4:55 PM
 **/
public class AESUtil {

    private static final String KEY_AES = "AES";

    /**
     * 算法
     */
    private static final String ALGORITHMSTR = "AES/ECB/PKCS5Padding";

    private static final String CHARSET = "UTF-8";

    public static String createKeys() {
        //返回生成指定算法密钥生成器的 KeyGenerator 对象
        KeyGenerator kg = null;
        try {
            kg = KeyGenerator.getInstance(KEY_AES);
            //AES 要求密钥长度为 128
            kg.init(128);
            //生成一个密钥
            SecretKey secretKey = kg.generateKey();
            return Base64.encodeBase64URLSafeString(secretKey.getEncoded());
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * AES加密为base 64 code
     *
     * @param content    待加密的内容
     * @param encryptKey 加密密钥
     * @return 加密后的base 64 code
     */
    public static String aesEncrypt(String content, String encryptKey) throws Exception {
        KeyGenerator kgen = KeyGenerator.getInstance("AES");
        kgen.init(128);
        Cipher cipher = Cipher.getInstance(ALGORITHMSTR);
        byte[] keyBytes = Base64.decodeBase64(encryptKey);
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(keyBytes, "AES"));
        return Base64.encodeBase64String(cipher.doFinal(content.getBytes(CHARSET)));
    }


    /**
     * 将base 64 code AES解密
     *
     * @param encryptStr 待解密的base 64 code
     * @param decryptKey 解密密钥
     * @return 解密后的string
     */
    public static String aesDecrypt(String encryptStr, String decryptKey) throws Exception {
        if (encryptStr == null || encryptStr.length() == 0) {
            System.err.println(AESUtil.class.getName() + ", 解密出错，密文空");
        }
        byte[] encryptBytes = Base64.decodeBase64(encryptStr);
        KeyGenerator kgen = KeyGenerator.getInstance("AES");
        kgen.init(128);
        Cipher cipher = Cipher.getInstance(ALGORITHMSTR);
        byte[] keyBytes = Base64.decodeBase64(decryptKey);
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(keyBytes, "AES"));
        byte[] decryptBytes = cipher.doFinal(encryptBytes);
        return new String(decryptBytes);
    }
}
