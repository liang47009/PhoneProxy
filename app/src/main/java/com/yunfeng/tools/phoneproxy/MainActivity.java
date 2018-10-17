package com.yunfeng.tools.phoneproxy;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.yunfeng.tools.phoneproxy.adapter.NetworkSimpleAdapter;
import com.yunfeng.tools.phoneproxy.http.SocketProxy;
import com.yunfeng.tools.phoneproxy.listener.AdMobListener;
import com.yunfeng.tools.phoneproxy.listener.MyProxyEventListener;
import com.yunfeng.tools.phoneproxy.receiver.InternetChangeBroadcastReceiver;
import com.yunfeng.tools.phoneproxy.util.PermissionHelper;
import com.yunfeng.tools.phoneproxy.util.Utils;
import com.yunfeng.tools.phoneproxy.view.SettingsActivity;

import java.lang.ref.WeakReference;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final int MSG_INTERNETCHANGED = 1;

    private MyProxyEventListener listener;
    private InternetChangeBroadcastReceiver receiver;
    private NetworkSimpleAdapter simpleAdapter = null;
    private static final SocketProxy socketProxy = new SocketProxy();

    private static final MainHandler handler = new MainHandler();

    private static class MainHandler extends Handler {
        private WeakReference<MainActivity> weakReference;

        private void setWeakReference(MainActivity mainActivity) {
            weakReference = new WeakReference<>(mainActivity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            MainActivity activity = weakReference.get();
            if (null != activity) {
                switch (msg.what) {
                    case MSG_INTERNETCHANGED:
                        activity.simpleAdapter.notifyDataSetChanged();
                        break;
                }
            }
        }
    }

    public static Handler getHandler() {
        return handler;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        handler.setWeakReference(this);
        Utils.initFireBase(this);
        PermissionHelper.request(this);
        initView(this);
        //https://phoneproxy.tools.yunfeng.com/.well-known/assetlinks.json
        // ATTENTION: This was auto-generated to handle app links.
        Intent appLinkIntent = this.getIntent();
        String appLinkAction = appLinkIntent.getAction();
        Uri appLinkData = appLinkIntent.getData();
        listener = new MyProxyEventListener(this);
        if (socketProxy.isStartUp()) {
            View view = this.findViewById(R.id.start_proxy);
            view.setEnabled(false);
        }
    }

    private void initView(Activity activity) {
        // Load an ad into the AdMob banner view.
        AdView adView = (AdView) activity.findViewById(R.id.adView);
        adView.setAdListener(new AdMobListener());
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
        simpleAdapter = new NetworkSimpleAdapter(activity, Utils.listems, R.layout.network_list, new String[]{"name"}, new int[]{R.id.tv_ips});
        ListView listView = (ListView) activity.findViewById(R.id.list_ips);
        listView.setAdapter(simpleAdapter);
        listView.setItemsCanFocus(false);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                ClipboardManager clipboardManager = (ClipboardManager) view.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                if (null != clipboardManager) {
                    String ip = ((TextView) view).getText().toString();
                    clipboardManager.setPrimaryClip(ClipData.newPlainText("text", ip));
                    Toast.makeText(view.getContext(), R.string.copy_to_clipboard, Toast.LENGTH_LONG).show();
                }
                return true;
            }
        });
        listView.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (null == receiver) {
            receiver = new InternetChangeBroadcastReceiver();
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(receiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // ATTENTION: This was auto-generated to handle app links.
        String appLinkAction = intent.getAction();
        Uri appLinkData = intent.getData();
    }

    @Override
    protected void onDestroy() {
        socketProxy.onDestory(this);
        super.onDestroy();
    }

    @Override
    @TargetApi(Build.VERSION_CODES.M)
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        PermissionHelper.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intent = new Intent(this.getApplicationContext(), SettingsActivity.class);
            this.startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void stopProxy(View view) {
        socketProxy.onDestory(this);
    }

//    NettyServer nettyServer = new NettyServer();

    public void startProxy(View view) {
        final String port = PreferenceManager.getDefaultSharedPreferences(this).getString("default_porxy_port", "8888");
        socketProxy.startup(port, listener);
//        nettyServer.startUp("0.0.0.0", Integer.valueOf(port));
        ((Button) view).setText("Bind Port:" + port);
//        Intent it = new Intent(this, DataStreamService.class);
//        startService(it);
//        view.setEnabled(false);
    }

    public void clearLog(View view) {
        EditText logEditTextView = (EditText) MainActivity.this.findViewById(R.id.log_editText);
        logEditTextView.setText("");
    }

    public void changeIcon() {
        PackageManager pm = this.getPackageManager();
        if (pm.getComponentEnabledSetting(new ComponentName(this, "com.yunfeng.tools.phoneproxy.MainActivity")) == PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
            pm.setComponentEnabledSetting(new ComponentName(this, "com.yunfeng.tools.phoneproxy.MainActivity"), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
            pm.setComponentEnabledSetting(new ComponentName(this, "com.yunfeng.tools.phoneproxy.RoundActivity"), PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
        } else {
            pm.setComponentEnabledSetting(new ComponentName(this, "com.yunfeng.tools.phoneproxy.MainActivity"), PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
            pm.setComponentEnabledSetting(new ComponentName(this, "com.yunfeng.tools.phoneproxy.RoundActivity"), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        }
        Log.e("TAG", "setIcon----success!!");
    }

    public void restartSystemLauncher(PackageManager pm) {
        ActivityManager am = (ActivityManager) getSystemService(Activity.ACTIVITY_SERVICE);
        Intent i = new Intent(Intent.ACTION_MAIN);
        i.addCategory(Intent.CATEGORY_HOME);
        i.addCategory(Intent.CATEGORY_DEFAULT);
        List<ResolveInfo> resolves = pm.queryIntentActivities(i, 0);
        for (ResolveInfo res : resolves) {
            if (res.activityInfo != null) {
                if (am != null) {
                    am.killBackgroundProcesses(res.activityInfo.packageName);
                }
            }
        }
    }

}
