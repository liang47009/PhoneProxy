package com.yunfeng.tools.phoneproxy;


/**
 * log
 * Created by xll on 2017/12/5.
 */
public class Log {

    public static void d(Object msg) {
        android.util.Log.d("PhoneProxy", msg.toString());
    }

    public static void e(Object msg, Throwable cause) {
        android.util.Log.d("PhoneProxy", msg.toString(), cause);
    }
}
