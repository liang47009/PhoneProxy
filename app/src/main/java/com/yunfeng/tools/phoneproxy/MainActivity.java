package com.yunfeng.tools.phoneproxy;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.yunfeng.tools.phoneproxy.http.SocketProxy;
import com.yunfeng.tools.phoneproxy.util.Logger;
import com.yunfeng.tools.phoneproxy.view.DataEventObject;
import com.yunfeng.tools.phoneproxy.view.ProxyEvent;
import com.yunfeng.tools.phoneproxy.view.ProxyEventListener;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements ProxyEventListener {

    private static final int REQUEST_PERMISIONS = 0xffffff;

    private static final String[] permissions = new String[]{Manifest.permission.INTERNET};
    private final List<Map<String, Object>> listems = new ArrayList<Map<String, Object>>();
    private long totalUpStream;
    private long totalDownStream;
    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "1");
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "onCreate");
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "image");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

        MobileAds.initialize(this, "ca-app-pub-9683268735381992~5860363867");

        // Load an ad into the AdMob banner view.
        AdView adView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                // Code to be executed when an ad finishes loading.
                Logger.d("onAdLoaded");
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                // Code to be executed when an ad request fails.
                Logger.d("onAdFailedToLoad: " + errorCode);
            }

            @Override
            public void onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen.
                Logger.d("onAdOpened");
            }

            @Override
            public void onAdLeftApplication() {
                // Code to be executed when the user has left the app.
                Logger.d("onAdLeftApplication");
            }

            @Override
            public void onAdClosed() {
                // Code to be executed when when the user is about to return
                // to the app after tapping on an ad.
                Logger.d("onAdClosed");
            }
        });
        ListView listView = (ListView) findViewById(R.id.list_ips);

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
        final SimpleAdapter simplead = new SimpleAdapter(this, listems,
                R.layout.network_list, new String[]{"name", "head", "desc"},
                new int[]{R.id.name, R.id.head, R.id.desc});
        listView.setAdapter(simplead);
        listView.setItemsCanFocus(false);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listView.setVisibility(View.VISIBLE);
        addListData();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (String permission : permissions) {
                int granted = checkSelfPermission(permission);
                if (granted == PackageManager.PERMISSION_DENIED) {
                    requestPermissions(permissions, REQUEST_PERMISIONS);
                }
            }
        }
        //https://phoneproxy.tools.yunfeng.com/.well-known/assetlinks.json
        // ATTENTION: This was auto-generated to handle app links.
        Intent appLinkIntent = getIntent();
        String appLinkAction = appLinkIntent.getAction();
        Uri appLinkData = appLinkIntent.getData();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // ATTENTION: This was auto-generated to handle app links.
        String appLinkAction = intent.getAction();
        Uri appLinkData = intent.getData();
    }

    @Override
    @TargetApi(Build.VERSION_CODES.M)
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISIONS:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    requestPermissions(permissions, REQUEST_PERMISIONS);
                }
                break;
        }
    }

    private void addListData() {
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
                            Map<String, Object> listem = new HashMap<String, Object>();
                            listem.put("head", android.R.drawable.btn_default);
                            listem.put("name", hostName);
                            listem.put("desc", addr);
                            listems.add(listem);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void startProxy(View view) {
        view.setEnabled(false);
        SocketProxy.startup(this);
    }

    @Override
    public void onEvent(final ProxyEvent event) {
        if (event.getEventType() == ProxyEvent.EventType.LOG_EVENT) {
            this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    EditText view = (EditText) MainActivity.this.findViewById(R.id.log_editText);
                    view.append(event.getData().toString());
                }
            });
        } else if (event.getEventType() == ProxyEvent.EventType.DATA_EVENT) {
            this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    DataEventObject data = (DataEventObject) event.getData();
                    MainActivity.this.totalUpStream += data.getUpStream();
                    MainActivity.this.totalDownStream += data.getDownStream();
                    TextView view = (TextView) MainActivity.this.findViewById(R.id.data_textView);
                    view.setText(String.format(Locale.CHINA, "TotalUpStream: %d \r\n TotalDownStream: %d",
                            MainActivity.this.totalUpStream, MainActivity.this.totalDownStream));
                }
            });
        }
    }

    public void clearLog(View view) {
        EditText logEditTextView = (EditText) MainActivity.this.findViewById(R.id.log_editText);
        logEditTextView.setText("");
    }
}
