package com.yunfeng.tools.phoneproxy.util;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;

import com.google.android.gms.ads.MobileAds;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.yunfeng.tools.phoneproxy.MainActivity;
import com.yunfeng.tools.phoneproxy.view.fragment.ProxyFragment;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
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

    public static void changeIcon(Context context) {
        if (context != null) {
            PackageManager pm = context.getPackageManager();
            if (pm.getComponentEnabledSetting(new ComponentName(context, "com.yunfeng.tools.phoneproxy.MainActivity")) == PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
                pm.setComponentEnabledSetting(new ComponentName(context, "com.yunfeng.tools.phoneproxy.MainActivity"), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
                pm.setComponentEnabledSetting(new ComponentName(context, "com.yunfeng.tools.phoneproxy.RoundActivity"), PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
            } else {
                pm.setComponentEnabledSetting(new ComponentName(context, "com.yunfeng.tools.phoneproxy.MainActivity"), PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
                pm.setComponentEnabledSetting(new ComponentName(context, "com.yunfeng.tools.phoneproxy.RoundActivity"), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
            }
        } else {
            Logger.e("changeIcon context is null");
        }
    }

    public static void restartSystemLauncher(Context context) {
        if (context != null) {
            ActivityManager am = (ActivityManager) context.getSystemService(Activity.ACTIVITY_SERVICE);
            Intent i = new Intent(Intent.ACTION_MAIN);
            i.addCategory(Intent.CATEGORY_HOME);
            i.addCategory(Intent.CATEGORY_DEFAULT);
            PackageManager pm = context.getPackageManager();
            List<ResolveInfo> resolves = pm.queryIntentActivities(i, 0);
            for (ResolveInfo res : resolves) {
                if (res.activityInfo != null) {
                    if (am != null) {
                        am.killBackgroundProcesses(res.activityInfo.packageName);
                    }
                }
            }
        } else {
            Logger.e("changeIcon context is null");
        }
    }

    /*
     * 判断服务是否启动,context上下文对象 ，className服务的name
     */
    public static boolean isServiceRunning(Context mContext, String className) {
        boolean isRunning = false;
        ActivityManager activityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> serviceList = null;
        if (activityManager != null) {
            serviceList = activityManager.getRunningServices(100);

            if (!(serviceList.size() > 0)) {
                return false;
            }
            for (ActivityManager.RunningServiceInfo info : serviceList) {
                String temp = info.service.getClassName();
                if (temp.contains(className)) {
                    isRunning = true;
                    break;
                }
            }
        }
        return isRunning;
    }

    public static final int INTERNET_CHANGED = 0x11;

    public static void internetChange(Context context, Intent intent) {
        if (context instanceof MainActivity) {
            Logger.d("internetChanged!");
        }
        ProxyFragment.handler.sendEmptyMessage(INTERNET_CHANGED);
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

    public static void updateViewModel(final ProxyFragment fragment) {
        ThreadPool.getInstance().submit(new ThreadPool.Job<List<Map<String, Object>>>() {
            @Override
            public List<Map<String, Object>> run(ThreadPool.JobContext jc) {
                List<Map<String, Object>> listems = new ArrayList<Map<String, Object>>();
                try {
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
                } catch (SocketException e) {
                    e.printStackTrace();
                }
                return listems;
            }
        }, new FutureListener<List<Map<String, Object>>>() {
            @Override
            public void onFutureDone(Future<List<Map<String, Object>>> future) {
//                mViewModel.getListItems().setValue(future.get());
                fragment.setNetworkInterface(future.get());
            }
        });
    }
}
