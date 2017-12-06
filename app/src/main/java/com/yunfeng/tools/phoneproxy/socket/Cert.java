package com.yunfeng.tools.phoneproxy.socket;

import android.content.Context;

import com.yunfeng.tools.phoneproxy.MainActivity;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class Cert {

    private static Map<String, X509Certificate> certCache = new HashMap<String, X509Certificate>();
    private static String issuer = null;
    private static PrivateKey caPriKey = null;
    public static PrivateKey serverPriKey = null;
    private static PublicKey serverPubKey = null;

    public static void init(Context context) {
        try {
            Security.addProvider(new BouncyCastleProvider());
            InputStream cin = context.getAssets().open("c");
            // 读取CA证书使用者信息
            issuer = CertUtil.getSubject(cin);
            InputStream pin = context.getAssets().open("p");
            // CA私钥用于给动态生成的网站SSL证书签证
            caPriKey = CertUtil.loadPriKey(pin);
            // 生产一对随机公私钥用于网站SSL证书动态创建
            KeyPair keyPair = CertUtil.genKeyPair();
            serverPriKey = keyPair.getPrivate();
            serverPubKey = keyPair.getPublic();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static X509Certificate getCert(String host) throws Exception {
        X509Certificate cert = null;
        if (host != null) {
            String key = host.trim().toLowerCase();
            if (certCache.containsKey(key)) {
                return certCache.get(key);
            } else {
                cert = CertUtil.genCert(issuer, serverPubKey, caPriKey, key);
                certCache.put(key, cert);
            }
        }
        return cert;
    }

}
