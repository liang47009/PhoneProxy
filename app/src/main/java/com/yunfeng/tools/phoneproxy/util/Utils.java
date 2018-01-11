package com.yunfeng.tools.phoneproxy.util;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.yunfeng.tools.phoneproxy.R;
import com.yunfeng.tools.phoneproxy.listener.AdMobListener;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * aa
 * Created by xll on 2018/1/8.
 */
public class Utils {

    public static final int REQUEST_PERMISIONS = 0xffffff;

    private static final String[] permissions = new String[]{Manifest.permission.INTERNET};
    private static final List<Map<String, Object>> listems = new ArrayList<Map<String, Object>>();
    private static FirebaseAnalytics mFirebaseAnalytics;

    public static void addListData(final Activity activity, final SimpleAdapter simpleAdapter) {
        new Thread(new Runnable() {
            @Override
            public void run() {
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
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            simpleAdapter.notifyDataSetChanged();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
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

    public static void init(final Activity activity) {
        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(activity);
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "1");
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "onCreate");
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "image");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

        MobileAds.initialize(activity, "ca-app-pub-9683268735381992~5860363867");
        // Load an ad into the AdMob banner view.
        AdView adView = (AdView) activity.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
        adView.setAdListener(new AdMobListener());
        ListView listView = (ListView) activity.findViewById(R.id.list_ips);

		/*SimpleAdapter的参数说明
         * 第一个参数 表示访问整个android应用程序接口，基本上所有的组件都需要
		 * 第二个参数表示生成一个Map(String ,Object)列表选项
		 * 第三个参数表示界面布局的id  表示该文件作为列表项的组件
		 * 第四个参数表示该Map对象的哪些key对应value来生成列表项
		 * 第五个参数表示来填充的组件 Map对象key对应的资源一依次填充组件 顺序有对应关系
		 * 注意的是map对象可以key可以找不到 但组件的必须要有资源填充  因为 找不到key也会返回null 其实就相当于给了一个null资源
		 * 下面的程序中如果 new String[] { "name", "head", "desc","name" } new int[] {R.id.name,R.id.head,R.id.desc,R.id.head}
		 * 这个head的组件会被name资源覆盖
		 * */
        final SimpleAdapter simplead = new SimpleAdapter(activity, listems, R.layout.network_list, new String[]{"name"}, new int[]{R.id.name});
        listView.setAdapter(simplead);
        listView.setItemsCanFocus(false);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listView.setVisibility(View.VISIBLE);
        Utils.addListData(activity, simplead);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (String permission : permissions) {
                int granted = activity.checkSelfPermission(permission);
                if (granted == PackageManager.PERMISSION_DENIED) {
                    activity.requestPermissions(permissions, REQUEST_PERMISIONS);
                }
            }
        }
    }
}
