package com.yunfeng.tools.phoneproxy.util;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TimeUtil {
    private static final SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd", Locale.CHINA);

    public static String formatTimeInMillis(long millis) {
        return df.format(new Date(millis));
    }
}
