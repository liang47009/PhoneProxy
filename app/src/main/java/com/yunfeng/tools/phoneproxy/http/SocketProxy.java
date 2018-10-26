package com.yunfeng.tools.phoneproxy.http;

import com.yunfeng.tools.phoneproxy.listener.ErrorEventObject;
import com.yunfeng.tools.phoneproxy.listener.IProxyEventTask;
import com.yunfeng.tools.phoneproxy.listener.ProxyEvent;
import com.yunfeng.tools.phoneproxy.listener.ProxyEventListener;
import com.yunfeng.tools.phoneproxy.util.Future;
import com.yunfeng.tools.phoneproxy.util.ThreadPool;
import com.yunfeng.tools.phoneproxy.util.Utils;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

import xiaofei.library.hermes.annotation.ClassId;
import xiaofei.library.hermes.annotation.MethodId;

/**
 * http 代理程序 *
 */
@ClassId("SocketProxy")
public class SocketProxy implements IProxyEventTask {
    private ServerSocket serverSocket = null;
    private Future<?> serverFuture = null;
    private String port;
    private boolean isRunning = false;
    private int bufferSize = 1;

    public SocketProxy(String port) {
        this.port = port;
    }

    @MethodId("isRunning")
    @Override
    public boolean isRunning() {
        return isRunning;
    }

    @MethodId("start")
    @Override
    public void start(final ProxyEventListener listener) {
        startup(listener);
    }

    private void startup(final ProxyEventListener listener) {
        serverFuture = ThreadPool.getInstance().submit(new ThreadPool.Job<Object>() {
            @Override
            public Object run(ThreadPool.JobContext jc) {
                if (serverSocket == null || serverSocket.isClosed()) {
                    try {
                        serverSocket = new ServerSocket(Integer.valueOf(port));
                        String time = Utils.formatDate(new Date());
                        listener.onEvent(new ProxyEvent(ProxyEvent.EventType.SERVER_START_EVENT, time + "," + port));
                        while (!serverSocket.isClosed()) {
                            isRunning = true;
                            Socket socket = serverSocket.accept();
                            socket.setKeepAlive(true);
                            //加入任务列表，等待处理
                            ThreadPool.getInstance().submit(new ProxyTask(socket, bufferSize, listener));
                        }
                    } catch (Exception e) {
                        listener.onEvent(new ProxyEvent(ProxyEvent.EventType.ERROR_EVENT, new ErrorEventObject(e.getMessage(), e)));
                    } finally {
                        isRunning = false;
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

    @MethodId("stop")
    @Override
    public void stop() {
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (serverFuture != null) {
            serverFuture.cancel();
        }
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

}