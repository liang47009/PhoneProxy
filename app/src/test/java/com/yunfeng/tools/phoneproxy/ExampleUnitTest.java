package com.yunfeng.tools.phoneproxy;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }


//        sun.security.jca.GetInstance.getInstance(type, getSpiClass(type), algorithm).toArray();
//        sun.security.jca.ProviderList var3 = Providers.getProviderList();
//        java.security.Provider.Service var4 = var3.getService(var0, var2);//var0 keystore var2 jks
//        ProviderList list = Providers.getFullProviderList();
//        ProviderList newList = ProviderList.insertAt(list, provider, position - 1);
//        newList.getIndex(providerName) + 1
//        try
//
//    {
//        Class clazzProvider = Class.forName("java.security.Provider");
//
//            Class clazzProviders = Class.forName("sun.security.jca.Providers");
//            Method methodgetFullProviderList = clazzProviders.getDeclaredMethod("getFullProviderList");
//            Object obj = methodgetFullProviderList.invoke(null);
//            Method methodGetService = obj.getClass().getDeclaredMethod("getService", String.class, String.class);
//            Object objService = methodGetService.invoke(obj, "KeyStore", "JKS");
//
//            Method methodinsertAt = obj.getClass().getDeclaredMethod("insertAt", obj.getClass(), clazzProvider, int.class);
//            BouncyCastleProvider providerBC = new BouncyCastleProvider();
//            obj = methodinsertAt.invoke(null, obj, providerBC, -1);

//            Method methodGetProviderList = clazzProviders.getDeclaredMethod("getProviderList");
//            obj = methodGetProviderList.invoke(null);
//            Method methodGetService = obj.getClass().getDeclaredMethod("getService", String.class, String.class);
//            obj = methodGetService.invoke(obj, "KeyStore", "JKS");

//            Class clazz = Class.forName("sun.security.jca.GetInstance");
//            Method method = clazz.getDeclaredMethod("getInstance", String.class, Class.class, String.class);
//            Class clazzSpi = Class.forName("java.security.KeyStoreSpi");
//            obj = method.invoke(null, "KeyStore", clazzSpi, "JKS");
//            Log.d(obj.toString());
//    } catch(
//    Exception e)
//
//    {
//        e.printStackTrace();
//    }
//        try {
//            KeyStore ks = KeyStore.getInstance("PKCS12");
//            InputStream ksIs = new FileInputStream("D:/client.p12");
//            try {
//                ks.load(ksIs, "a123456".toCharArray()); //用户证书密码
//            } finally {
//                if (ksIs != null) {
//                    ksIs.close();
//                }
//            }
//            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
//            kmf.init(ks, "a123456".toCharArray()); //用户证书密码
//            KeyStore ts = KeyStore.getInstance("BKS"); //or jks
//            InputStream tsIs = new FileInputStream("D:/server.bks");
//            try {
//                ts.load(tsIs, "123456".toCharArray()); //服务器证书密码
//            } finally {
//                if (tsIs != null) {
//                    tsIs.close();
//                }
//            }
//            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
//            tmf.init(ts);
//            SSLContext ctx = SSLContext.getInstance("SSLv3");
//            ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
}