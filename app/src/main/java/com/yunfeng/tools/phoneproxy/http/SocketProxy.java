package com.yunfeng.tools.phoneproxy.http;

import com.yunfeng.tools.phoneproxy.MainActivity;
import com.yunfeng.tools.phoneproxy.util.Logger;
import com.yunfeng.tools.phoneproxy.view.ProxyEventListener;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * http 代理程序 *
 */
public class SocketProxy {
    private static final int listenPort = 8888;
    private static final ExecutorService executorService = Executors.newCachedThreadPool();

    public static void startup(final ProxyEventListener listener) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.CHINA);
                    ServerSocket serverSocket = new ServerSocket(listenPort);
                    Logger.d("Proxy Server Start At" + sdf.format(new Date()));
                    Logger.d("listening port:" + listenPort + "……");
                    while (true) {
                        try {
                            Socket socket = serverSocket.accept();
                            socket.setKeepAlive(true);
                            //加入任务列表，等待处理
                            executorService.execute(new ProxyTask(socket, listener));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}