package com.yunfeng.tools.phoneproxy.http;

import android.util.Log;

import com.yunfeng.tools.phoneproxy.listener.DataEventObject;
import com.yunfeng.tools.phoneproxy.listener.ProxyEvent;
import com.yunfeng.tools.phoneproxy.listener.ProxyEventListener;
import com.yunfeng.tools.phoneproxy.util.Const;
import com.yunfeng.tools.phoneproxy.util.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Date;

import static com.yunfeng.tools.phoneproxy.http.ProxyTask.StreamType.DOWNSTREAM;
import static com.yunfeng.tools.phoneproxy.http.ProxyTask.StreamType.UPSTREAM;

/**
 * 将客户端发送过来的数据转发给请求的服务器端，并将服务器返回的数据转发给客户端 *
 */
public final class ProxyTask implements Runnable {
    private Socket socketIn;
    private Socket socketOut;
    private long totalUpload = 0l;//总计上行比特数
    private long totalDownload = 0l;//总计下行比特数
    private ProxyEventListener listener;

    public ProxyTask(Socket socket, ProxyEventListener listener) {
        this.socketIn = socket;
        this.listener = listener;
    }

    private StringBuilder builder = new StringBuilder();

    private DataEventObject data = new DataEventObject();

    @Override
    public void run() {
        try {
            builder.append("\r\n").append("-----------------start------------");
            builder.append("\r\n").append("Request Time: ").append(Utils.formatDate(new Date()));
            InputStream isIn = socketIn.getInputStream();
            OutputStream osIn = socketIn.getOutputStream();  //从客户端流数据中读取头部，获得请求主机和端口
            Log.e("PP", "-----------------------------------------------------------");
            HttpHeader header = HttpHeader.readHeader(isIn); //添加请求日志信息
            builder.append("\r\n").append("From Host: ").append(socketIn.getInetAddress());
            builder.append("\r\n").append("From Port: ").append(socketIn.getPort());
            builder.append("\r\n").append("Proxy Method: ").append(header.getMethod());
            builder.append("\r\n").append("Request Host: ").append(header.getHost());
            builder.append("\r\n").append("Request Port: ").append(header.getPort());
            builder.append("\r\n").append("----------------------------------");
            listener.onEvent(new ProxyEvent(ProxyEvent.EventType.LOG_EVENT, builder.toString()));
            //如果没解析出请求请求地址和端口，则返回错误信息
            Log.e("PP", "-----------------------------------------------------------");
            if (header.getHost() == null || header.getPort() == null) {
                osIn.write(Const.SERVERERROR.getBytes());
                osIn.flush();
                return;
            }
            // 查找主机和端口
            socketOut = new Socket(header.getHost(), Integer.parseInt(header.getPort()));
            socketOut.setKeepAlive(true);
            socketOut.setSoTimeout(6 * 1000);
            InputStream isOut = socketOut.getInputStream();
            OutputStream osOut = socketOut.getOutputStream();
            //新开一个线程将返回的数据转发给客户端,串行会出问题，尚没搞明白原因
            Thread ot = new Thread(new ReadWriteThread(isOut, osIn, DOWNSTREAM));
            ot.start();
            if (header.getMethod().equals(HttpHeader.METHOD_CONNECT)) {
                // 将已联通信号返回给请求页面
                osIn.write(Const.AUTHORED.getBytes());
                osIn.flush();
            } else {
                //http请求需要将请求头部也转发出去
                byte[] headerData = header.toString().getBytes();
                totalUpload += headerData.length;
                osOut.write(headerData);
                osOut.flush();
            }
            //读取客户端请求过来的数据转发给服务器
            new Thread(new ReadWriteThread(isIn, osOut, DOWNSTREAM)).start();
            //等待向客户端转发的线程结束
            ot.join();
        } catch (Exception e) {
            listener.onEvent(new ProxyEvent(ProxyEvent.EventType.LOG_EVENT, "ProxyTask run(); error" + e.getMessage()));
            if (!socketIn.isOutputShutdown()) {
                //如果还可以返回错误状态的话，返回内部错误
                try {
                    socketIn.getOutputStream().write(Const.SERVERERROR.getBytes());
                } catch (IOException e1) {
                    listener.onEvent(new ProxyEvent(ProxyEvent.EventType.LOG_EVENT, "socketIn.getOutputStream().write(Const.SERVERERROR.getBytes()); error" + e.getMessage()));
                }
            }
        } finally {
            try {
                if (socketIn != null) {
                    socketIn.close();
                }
            } catch (IOException e) {
                listener.onEvent(new ProxyEvent(ProxyEvent.EventType.LOG_EVENT, "socketIn.close(); error" + e.getMessage()));
            }
            if (socketOut != null) {
                try {
                    socketOut.close();
                } catch (IOException e) {
                    listener.onEvent(new ProxyEvent(ProxyEvent.EventType.LOG_EVENT, "socketOut.close(); error" + e.getMessage()));
                }
            }
            //纪录上下行数据量和最后结束时间并打印
            builder.append("\r\n").append("Up Bytes: ").append(totalUpload);
            builder.append("\r\n").append("Down Bytes: ").append(totalDownload);
            builder.append("\r\n").append("Closed Time: ").append(Utils.formatDate(new Date()));
            builder.append("\r\n").append("------------end---------------\r\n");
            listener.onEvent(new ProxyEvent(ProxyEvent.EventType.LOG_EVENT, builder.toString()));
        }
    }

    enum StreamType {
        UPSTREAM, DOWNSTREAM
    }

    class ReadWriteThread implements Runnable {
        InputStream in;
        OutputStream out;
        StreamType streamType;

        ReadWriteThread(InputStream in, OutputStream out, StreamType streamType) {
            this.in = in;
            this.out = out;
            this.streamType = streamType;
        }

        @Override
        public void run() {
            byte[] buffer = new byte[1024];
            try {
                int len;
                while ((len = in.read(buffer)) != -1) {
                    if (len > 0) {
                        out.write(buffer, 0, len);
                        out.flush();
                        if (streamType == UPSTREAM) {
                            totalUpload += len;
                            data.setUpStream(totalUpload);
                        } else if (streamType == DOWNSTREAM) {
                            totalDownload += len;
                            data.setDownStream(totalDownload);
                        }
                        listener.onEvent(new ProxyEvent(ProxyEvent.EventType.DATA_EVENT, data));
                    }
                }
            } catch (Exception e) {
                listener.onEvent(new ProxyEvent(ProxyEvent.EventType.LOG_EVENT, "ReadWriteThread error" + e.getMessage()));
            }
        }
    }

}