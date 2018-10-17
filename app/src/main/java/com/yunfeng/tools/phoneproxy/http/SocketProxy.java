package com.yunfeng.tools.phoneproxy.http;

import android.app.Activity;

import com.yunfeng.tools.phoneproxy.listener.ErrorEventObject;
import com.yunfeng.tools.phoneproxy.listener.ProxyEvent;
import com.yunfeng.tools.phoneproxy.listener.ProxyEventListener;
import com.yunfeng.tools.phoneproxy.util.Future;
import com.yunfeng.tools.phoneproxy.util.ThreadPool;
import com.yunfeng.tools.phoneproxy.util.Utils;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

/**
 * http 代理程序 *
 */
public class SocketProxy {
    private ServerSocket serverSocket = null;
    private Future<?> serverFuture = null;

    public void startup(final String port, final ProxyEventListener listener) {
        serverFuture = ThreadPool.getInstance().submit(new ThreadPool.Job<Object>() {
            @Override
            public Object run(ThreadPool.JobContext jc) {
                if (serverSocket == null || serverSocket.isClosed()) {
                    try {
                        serverSocket = new ServerSocket(Integer.valueOf(port));
                        String time = Utils.formatDate(new Date());
                        listener.onEvent(new ProxyEvent(ProxyEvent.EventType.SERVER_START_EVENT, time + "," + port));
                        while (!serverSocket.isClosed()) {
                            Socket socket = serverSocket.accept();
                            socket.setKeepAlive(true);
                            //加入任务列表，等待处理
                            ThreadPool.getInstance().submit(new ProxyTask(socket, listener));
                        }
                    } catch (Exception e) {
                        listener.onEvent(new ProxyEvent(ProxyEvent.EventType.ERROR_EVENT, new ErrorEventObject(e.getMessage(), e)));
                    } finally {
                        listener.onEvent(new ProxyEvent(ProxyEvent.EventType.SERVER_STOP_EVENT, null));
                    }
                }
                return null;
            }
        });
    }

    public boolean isStartUp() {
        boolean starting = false;
        if (serverSocket != null && !serverSocket.isClosed()) {
            starting = true;
        }
        return starting;
    }

    public void onDestory(Activity activity) {
        if (serverFuture != null) {
            serverFuture.cancel();
        }
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}