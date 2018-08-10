package com.yunfeng.tools.phoneproxy.util;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.widget.SimpleAdapter;

import com.google.android.gms.ads.MobileAds;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.yunfeng.tools.phoneproxy.MainActivity;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * aa
 * Created by xll on 2018/1/8.
 */
public class Utils {

    public static final List<Map<String, Object>> listems = new ArrayList<Map<String, Object>>();

    private final static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.CHINA);

    public static void internetChange(Context context) {
        ThreadPool.getInstance().submit(new ThreadPool.Job<Object>() {
            @Override
            public Object run(ThreadPool.JobContext jc) throws Exception {
                synchronized (listems) {
                    listems.clear();
                    Enumeration<NetworkInterface> nis = NetworkInterface.getNetworkInterfaces();
                    while (nis.hasMoreElements()) {
                        NetworkInterface ni = nis.nextElement();
                        Enumeration<InetAddress> ias = ni.getInetAddresses();
                        while (ias.hasMoreElements()) {
                            InetAddress ia = ias.nextElement();
                            String addr = ia.getHostAddress();
                            String hostName = ia.getHostName();
                            Logger.d("addr: " + addr + ", hostName: " + hostName);
                            if (!checkDataExsit("name", addr)) {
                                Map<String, Object> listem = new HashMap<String, Object>();
                                listem.put("name", addr);
                                listems.add(listem);
                            }
                        }
                    }
                }
                MainActivity.getHandler().sendEmptyMessage(MainActivity.MSG_INTERNETCHANGED);
                return null;
            }
        });
    }

    private static boolean checkDataExsit(Object key, Object value) {
        if (key instanceof String) {
            String strKey = (String) key;
            for (Map<String, Object> ips : listems) {
                Object tempValue = ips.get(strKey);
                if (null != tempValue) {
                    if (tempValue.toString().equalsIgnoreCase(value.toString())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static void initFireBase(final Activity activity) {
        // Obtain the FirebaseAnalytics instance.
        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(activity);
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "1");
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "onCreate");
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "image");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
        MobileAds.initialize(activity, "ca-app-pub-9683268735381992~5860363867");
    }

    public static String formatDate(Date date) {
        return sdf.format(date);
    }
}
