package com.yunfeng.tools.phoneproxy.http;

import com.yunfeng.tools.phoneproxy.listener.ErrorEventObject;
import com.yunfeng.tools.phoneproxy.listener.ProxyEvent;
import com.yunfeng.tools.phoneproxy.util.Logger;
import com.yunfeng.tools.phoneproxy.listener.ProxyEventListener;

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
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private ServerSocket serverSocket = null;

    public void startup(final String port, final ProxyEventListener listener) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.CHINA);
                    serverSocket = new ServerSocket(Integer.valueOf(port));
                    Logger.d("Proxy Server Start At" + sdf.format(new Date()));
                    Logger.d("listening port:" + port + "……");
                    listener.onEvent(new ProxyEvent(ProxyEvent.EventType.SERVER_START_EVENT, null));
                    while (true) {
                        Socket socket = serverSocket.accept();
                        socket.setKeepAlive(true);
                        //加入任务列表，等待处理
                        executorService.execute(new ProxyTask(socket, listener));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    listener.onEvent(new ProxyEvent(ProxyEvent.EventType.ERROR_EVENT, new ErrorEventObject("server error", e)));
                } finally {
                    listener.onEvent(new ProxyEvent(ProxyEvent.EventType.SERVER_STOP_EVENT, null));
                }
            }
        });
    }

    public boolean isStartUp() {
        boolean starting = false;
        if (serverSocket != null && serverSocket.isBound()) {
            starting = true;
        }
        return starting;
    }
}