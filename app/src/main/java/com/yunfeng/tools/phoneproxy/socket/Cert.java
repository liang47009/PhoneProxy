package com.yunfeng.tools.phoneproxy.socket;

import android.content.Context;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.InputStream;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

public class Cert {

    private static Map<String, X509Certificate> certCache = new HashMap<String, X509Certificate>();
    private static String issuer = null;
    private static PrivateKey caPriKey = null;
    public static PrivateKey serverPriKey = null;
    private static PublicKey serverPubKey = null;

    public static final BouncyCastleProvider provider = new BouncyCastleProvider();

    public static void init(Context context) {
        try {
//            Set<Provider.Service> services = provider.getServices();
//            Log.d("before addProvider: " + services.size());
            int i = Security.addProvider(provider);
//            services = provider.getServices();
//            Log.d("after addProvider: " + services.size());
//            Provider[] providers = Security.getProviders();
//            for (Provider provider1 : providers) {
//                for (Map.Entry<Object, Object> entry : provider1.entrySet()) {
//                    Log.d("entry.key = " + entry.getKey() + "======" + "entry.value = " + entry.getValue());
//                }
//            }

            // 生产一对随机公私钥用于网站SSL证书动态创建
            KeyPair keyPair = CertUtil.genKeyPair();
            serverPriKey = keyPair.getPrivate();
            serverPubKey = keyPair.getPublic();

            InputStream cin = context.getAssets().open("c");
            // 读取CA证书使用者信息
            issuer = CertUtil.getSubject(cin);
            InputStream pin = context.getAssets().open("p");
            // CA私钥用于给动态生成的网站SSL证书签证
            caPriKey = CertUtil.loadPriKey(pin);
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
