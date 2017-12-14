package com.yunfeng.tools.phoneproxy.util;

import android.content.Context;

import org.bouncycastle.util.encoders.Base64Encoder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.util.Enumeration;

/**
 * Utils
 * Created by xll on 2017/12/14.
 */
public class Utils {

    public static KeyPair getPrivateKey(KeyStore keystore, String alias, char[] password) {
        try {
            Key key = keystore.getKey(alias, password);
            if (key instanceof PrivateKey) {
                Certificate cert = keystore.getCertificate(alias);
                PublicKey publicKey = cert.getPublicKey();
                return new KeyPair(publicKey, (PrivateKey) key);
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    public static void export(Context context, String type, String alias, char[] password, String outFilePath) {
        try {
            KeyStore keystore = KeyStore.getInstance(type);
            InputStream in = context.getAssets().open("bksmy.keystore");
            keystore.load(in, password);
            KeyPair keyPair = getPrivateKey(keystore, alias, password);
            PrivateKey privateKey = keyPair.getPrivate();
            FileOutputStream writer = new FileOutputStream(
                    new File(outFilePath));
            Base64Encoder encoder = new Base64Encoder();
            int encoded = encoder.encode(privateKey.getEncoded(), 0,
                    privateKey.getEncoded().length, writer);
            System.out.println("size:" + encoded);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static String getLocalIpAddress() {
        try {
            Enumeration<NetworkInterface> ens = NetworkInterface.getNetworkInterfaces();
            while (ens.hasMoreElements()) {
                NetworkInterface networkInterface = ens.nextElement();
                Enumeration<InetAddress> ias = networkInterface.getInetAddresses();
                while (ias.hasMoreElements()) {
                    InetAddress inetAddress = ias.nextElement();
                    if (!inetAddress.isLoopbackAddress() && (inetAddress instanceof Inet4Address)) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return null;
    }

}
