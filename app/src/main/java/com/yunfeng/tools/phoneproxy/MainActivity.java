package com.yunfeng.tools.phoneproxy;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.ads.MobileAds;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.yunfeng.tools.phoneproxy.receiver.InternetChangeBroadcastReceiver;
import com.yunfeng.tools.phoneproxy.util.GenericHandler;
import com.yunfeng.tools.phoneproxy.util.Logger;
import com.yunfeng.tools.phoneproxy.util.NativeColor;
import com.yunfeng.tools.phoneproxy.util.PermissionHelper;
import com.yunfeng.tools.phoneproxy.util.PermissionRequest;
import com.yunfeng.tools.phoneproxy.view.SettingsActivity;
import com.yunfeng.tools.phoneproxy.view.fragment.FileSelectFragment;
import com.yunfeng.tools.phoneproxy.view.fragment.ProxyFragment;
import com.yunfeng.tools.phoneproxy.view.fragment.RemoteManagerFragment;
import com.yunfeng.tools.phoneproxy.view.fragment.SettingsFragment;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import xiaofei.library.hermes.Hermes;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, GenericHandler.MessageCallback {

    public static final int MSG_INVALIDATION = 0x11;
    public static final String PROXY_FRAGMENT = "ProxyFragment";
    public static final String REMOTE_MANAGER_FRAGMENT = "RemoteManagerFragment";
    public static final String SETTINGS_FRAGMENT = "SettingsFragment";
    private InternetChangeBroadcastReceiver receiver;
    private Map<String, Fragment> fragments = new HashMap<>(4);
    private String color;
    private Boolean enableChangeSkin = false;

    private List<String> viewPrefixList = new ArrayList<>(4);

    {
        viewPrefixList.add("android.view.");
        viewPrefixList.add("android.widget.");
        viewPrefixList.add("android.webkit.");
    }

    private static GenericHandler handler = new GenericHandler();

    public static Handler getHandler() {
        return handler;
    }

    private static final String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

    @Override
    public View onCreateView(String name, Context context, AttributeSet attrs) {
        View view = null;
        if (enableChangeSkin) {// enable skin change
            for (String prefix : viewPrefixList) {
                try {
                    if (-1 == name.indexOf('.')) {
                        view = LayoutInflater.from(context).createView(name, prefix, attrs);
                    } else {
                        view = LayoutInflater.from(context).createView(name, null, attrs);
                    }
                } catch (Exception e) {
                    Logger.e("onCreateView error: " + e.getMessage());
                }
                if (null != view) {
                    break;
                }
            }
            if (view != null) {
                String theme = attrs.getAttributeValue("http://schemas.android.com/apk/res-auto", "background");
                if (TextUtils.isEmpty(theme)) {
                    Integer id = NativeColor.getNativeColorByName(color);
                    if (null != id) {
                        view.setBackgroundColor(getResources().getColor(id));
                    }
                } else {
                    theme = theme.substring(1, theme.length());
                    view.setBackground(getResources().getDrawable(Integer.valueOf(theme)));
                }
            }
        }
        return view;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        updateConfig(this);
        setContentView(R.layout.activity_sidebar);
//        PermissionHelper.request(this);
        permissionRequest = new PermissionRequest(this);
        permissionCheck();
    }

    private void permissionCheck() {
        for (String per : permissions) {
            permissionRequest.startRequest(per, new PermissionRequest.Callback() {
                @Override
                public void onCallback(boolean result) {
                    if (!result) {
                        permissionCheck();
                    } else {
                        init();
                    }
                }
            });
        }
    }

    private PermissionRequest permissionRequest;

    @TargetApi(23)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        PermissionHelper.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
        permissionRequest.onResponse(requestCode, permissions, grantResults);
    }

    private void init() {
        MobileAds.initialize(this, "ca-app-pub-9683268735381992~5860363867");
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        handler.setWeakReference(new WeakReference<GenericHandler.MessageCallback>(this));
        this.getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                Logger.d("fragment back stack changed!");
            }
        });
//        com.google.android.gms.measurement.AppMeasurementService
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ProxyFragment pf = ProxyFragment.newInstance();
        fragments.put(PROXY_FRAGMENT, pf);
        fragments.put(REMOTE_MANAGER_FRAGMENT, RemoteManagerFragment.newInstance());
        fragments.put(SETTINGS_FRAGMENT, SettingsFragment.newInstance());
        Hermes.setHermesListener(pf);
        changeFragment(PROXY_FRAGMENT);

        showDialogInDifferentScreen();
    }

    private void updateConfig(Context context) {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        color = preferences.getString("example_skin_list", "white");
        enableChangeSkin = preferences.getBoolean("change_skin_switch", false);
    }

    public void showDialogInDifferentScreen() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FileSelectFragment newFragment = new FileSelectFragment();
//        boolean mIsLargeLayout = getResources().getBoolean(R.bool.large_layout);
        boolean mIsLargeLayout = true;
        Log.e("TAG", mIsLargeLayout + "");
        if (mIsLargeLayout) {
            // The device is using a large layout, so show the fragment as a
            // dialog
            newFragment.show(fragmentManager, "dialog");
        } else {
            // The device is smaller, so show the fragment fullscreen
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            // For a little polish, specify a transition animation
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            // To make it fullscreen, use the 'content' root view as the
            // container
            // for the fragment, which is always the root view for the activity
            transaction.replace(R.id.container, newFragment).commit();
        }
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
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.sidebar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.nav_proxy) {
            changeFragment(PROXY_FRAGMENT);
        } else if (id == R.id.nav_remote_manager) {
            changeFragment(REMOTE_MANAGER_FRAGMENT);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void changeFragment(String tag) {
        Fragment f = fragments.get(tag);
        if (null == f) {
            Logger.d("no fragment found with tag: " + tag);
        } else {
            FragmentTransaction ft = this.getSupportFragmentManager().beginTransaction();
            ft.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);
            ft.replace(R.id.container, f);
            ft.commit();
        }
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_INVALIDATION:
                this.recreate();
                break;
        }
    }
}
