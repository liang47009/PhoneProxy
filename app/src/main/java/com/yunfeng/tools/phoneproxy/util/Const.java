package com.yunfeng.tools.phoneproxy.util;

/**
 * const
 * Created by xll on 2018/10/16.
 */
public class Const {

    public static final String TAG = "app";

    /**
     * 已连接到请求的服务器
     */
    public static final String AUTHORED = "HTTP/1.1 200 Connection established\r\n\r\n";
    /**
     * 本代理登陆失败(此应用暂时不涉及登陆操作)
     */
    public static final String UNAUTHORED = "HTTP/1.1 407 Unauthorized\r\n\r\n";
    /**
     * 内部错误
     */
    public static final String SERVERERROR = "HTTP/1.1 500 Connection FAILED\r\n\r\n";

}
