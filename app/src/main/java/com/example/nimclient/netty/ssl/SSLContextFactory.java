package com.example.nimclient.netty.ssl;

import android.content.Context;
import android.content.res.AssetManager;

import java.security.KeyStore;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

public class SSLContextFactory {
    /**
     * 协议
     */
    private static final String PROTOCOL = "TLSv1.2";

    public static SSLContext getClientContext(Context context) {
        SSLContext clientContext = null;
        AssetManager am = context.getAssets();
        try {
            String keyStorePassword = "nijiaqi123";

            // 密钥BKS格式,JKS不能用
            KeyStore ks = KeyStore.getInstance("BKS");
            ks.load(am.open("ssl/kclient.bks"), keyStorePassword.toCharArray());

            // 默认SunX509
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(ks, keyStorePassword.toCharArray());

            // truststore
            KeyStore ts = KeyStore.getInstance("BKS");
            ts.load(am.open("ssl/tclient.bks"), keyStorePassword.toCharArray());

            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(ts);
            clientContext = SSLContext.getInstance(PROTOCOL);
            clientContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return clientContext;
    }
}
