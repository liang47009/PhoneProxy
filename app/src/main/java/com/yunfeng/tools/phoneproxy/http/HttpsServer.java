package com.yunfeng.tools.phoneproxy.http;

import com.yunfeng.tools.phoneproxy.Log;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.KeyStore;
import java.security.Security;

import javax.net.ServerSocketFactory;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.TrustManagerFactory;

/**
 * server Created by xll on 2017/12/8.
 */
public class HttpsServer {
    private static boolean b_exit = false;
    private static HttpsSocket httpsSocket;

    public static void startup(InputStream ksIn) {
        try {
            System.setProperty("javax.net.debug", "SSL,handshake,data,trustmanager");
            Security.addProvider(new BouncyCastleProvider());
            SSLContext context = SSLContext.getInstance("TLS");
            KeyStore ks = KeyStore.getInstance("BKS", "BC");
            ks.load(ksIn, "a123456".toCharArray());
            String algorithm = KeyManagerFactory.getDefaultAlgorithm();
            KeyManagerFactory kf = KeyManagerFactory.getInstance(algorithm);

            InputStream trustIn = HttpsServer.class.getClassLoader()
                    .getResourceAsStream("client.bks");
            KeyStore ksTrust = KeyStore.getInstance("BKS", "BC");
            ksTrust.load(trustIn, "a123456".toCharArray());
            TrustManagerFactory tmf = TrustManagerFactory
                    .getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(ksTrust);

            kf.init(ks, "a123456".toCharArray());
            context.init(kf.getKeyManagers(), tmf.getTrustManagers(), null);

            ServerSocketFactory factory = context.getServerSocketFactory();
            SSLServerSocket _socket = (SSLServerSocket) factory
                    .createServerSocket(8888);
            _socket.setNeedClientAuth(false);
            while (!b_exit) {
                httpsSocket = new HttpsSocket(_socket.accept());
                httpsSocket.start();
            }
        } catch (Exception e) {
            Log.d(e.getMessage());
        }

    }

    public static void stop() {
        b_exit = true;
        if (null != httpsSocket) {
            httpsSocket.interrupt();
        }
    }
}

/**
 * aa Created by xll on 2017/12/8.
 */
class HttpsSocket extends Thread {

    private Socket socket;

    public HttpsSocket(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    socket.getInputStream()));
            PrintWriter pw = new PrintWriter(socket.getOutputStream());
            String data = reader.readLine();
            Log.d("server recv: " + data);
            pw.println(data);
            pw.flush();
            pw.close();
            reader.close();
        } catch (IOException e) {
            Log.e(e.getMessage(), e);
            try {
                socket.close();
            } catch (IOException e1) {
                Log.d(e1.getMessage());
            }
        }
    }

}
