package com.yunfeng.tools.phoneproxy.util;


/**
 * log
 * Created by xll on 2017/12/5.
 */
public class Logger {

    public static void d(Object msg) {
        android.util.Log.d(Const.TAG, msg.toString());
    }

    public static void e(Object msg) {
        android.util.Log.e(Const.TAG, msg.toString());
    }

    public static void e(Object msg, Throwable cause) {
        android.util.Log.d(Const.TAG, msg.toString(), cause);
    }
}
