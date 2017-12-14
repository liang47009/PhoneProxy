//package com.yunfeng.tools.phoneproxy;
//
//import android.app.Activity;
//import android.os.Bundle;
//import android.view.Menu;
//
//import java.io.BufferedReader;
//import java.io.InputStreamReader;
//import java.net.URL;
//import java.security.KeyStore;
//
//import javax.net.ssl.HttpsURLConnection;
//import javax.net.ssl.KeyManagerFactory;
//import javax.net.ssl.SSLContext;
//import javax.net.ssl.SSLSocketFactory;
//import javax.net.ssl.TrustManagerFactory;
//
///**
// * acc
// * Created by xll on 2017/12/7.
// */
//public class CertActivity extends Activity {
//    private static final int SERVER_PORT = 50030;// 端口号
//    private static final String SERVER_IP = "218.206.176.146";// 连接IP
//    private static final String CLIENT_KET_PASSWORD = "123456";// 私钥密码
//    private static final String CLIENT_TRUST_PASSWORD = "123456";// 信任证书密码
//    private static final String CLIENT_AGREEMENT = "TLS";// 使用协议
//    private static final String CLIENT_KEY_MANAGER = "X509";// 密钥管理器
//    private static final String CLIENT_TRUST_MANAGER = "X509";//
//    private static final String CLIENT_KEY_KEYSTORE = "BKS";// 密库，这里用的是BouncyCastle密库
//    private static final String CLIENT_TRUST_KEYSTORE = "BKS";//
//    private static final String ENCONDING = "utf-8";// 字符集
//    private SSLSocketFactory sf;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//        try {
//            initKey();
//        } catch (Exception e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//    }
//
//    // 首先初始化客户端密钥以及客户端信任密钥库信息
//    private void initKey() throws Exception {
//        // 取得SSL的SSLContext实例
//        SSLContext sslContext = SSLContext.getInstance(CLIENT_AGREEMENT);
//        // 取得KeyManagerFactory实例
//        KeyManagerFactory keyManager = KeyManagerFactory
//                .getInstance(CLIENT_KEY_MANAGER);
//        // 取得TrustManagerFactory的X509密钥管理器
//        TrustManagerFactory trustManager = TrustManagerFactory
//                .getInstance(CLIENT_TRUST_MANAGER);
//
//        // 取得BKS密库实例
//        KeyStore clientKeyStore = KeyStore.getInstance("BKS");
//        KeyStore trustKeyStore = KeyStore.getInstance(CLIENT_TRUST_KEYSTORE);
//
//        // 加载证书和私钥,通过读取资源文件的方式读取密钥和信任证书（kclient:密钥;t_client:信任证书）
//
//        clientKeyStore.load(getResources().openRawResource(R.raw.tclient),
//                CLIENT_KET_PASSWORD.toCharArray());// kclient:密钥
//
//        // t_client:信任证书
//        trustKeyStore.load(getResources().openRawResource(R.raw.klient),
//                CLIENT_TRUST_PASSWORD.toCharArray());
//
//        // 初始化密钥管理器、信任证书管理器
//        keyManager.init(clientKeyStore, CLIENT_KET_PASSWORD.toCharArray());
//        trustManager.init(trustKeyStore);
//
//        // 初始化SSLContext
//        sslContext.init(keyManager.getKeyManagers(),
//                trustManager.getTrustManagers(),
//                new java.security.SecureRandom());
//        sf = sslContext.getSocketFactory();
//    }
//
//    // 访问服务器，获取响应数据
//    private String getData(String url) throws Exception {
//        HttpsURLConnection conn = (HttpsURLConnection) new URL(url)
//                .openConnection();
//        conn.setSSLSocketFactory(sf);
//        conn.setRequestMethod("GET");
//        conn.setConnectTimeout(10 * 1000);
//
//        conn.setDoOutput(true);
//        conn.setDoInput(true);
//        conn.connect();
//
//        BufferedReader br = new BufferedReader(new InputStreamReader(
//                conn.getInputStream()));
//        StringBuffer sb = new StringBuffer();
//        String line;
//        while ((line = br.readLine()) != null)
//            sb.append(line);
//        return sb.toString();
//
//    }
//}
