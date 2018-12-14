package com.yunfeng.tools.phoneproxy;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.yunfeng.tools.phoneproxy.receiver.InternetChangeBroadcastReceiver;
import com.yunfeng.tools.phoneproxy.util.GenericHandler;
import com.yunfeng.tools.phoneproxy.util.Logger;
import com.yunfeng.tools.phoneproxy.util.NativeColor;
import com.yunfeng.tools.phoneproxy.view.SettingsActivity;
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
                    view.setBackgroundDrawable(getResources().getDrawable(Integer.valueOf(theme)));
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
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        handler.setWeakReference(new WeakReference<GenericHandler.MessageCallback>(this));
        this.getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                Logger.d("fragment back stack changed!");
            }
        });

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
        fragments.put("ProxyFragment", pf);
        fragments.put("RemoteManagerFragment", RemoteManagerFragment.newInstance());
        fragments.put("SettingsFragment", SettingsFragment.newInstance());
        Hermes.setHermesListener(pf);
        changeFragment("ProxyFragment");
    }

    private void updateConfig(Context context) {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        color = preferences.getString("example_skin_list", "white");
        enableChangeSkin = preferences.getBoolean("change_skin_switch", false);
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
            changeFragment("ProxyFragment");
        } else if (id == R.id.nav_remote_manager) {
            changeFragment("RemoteManagerFragment");
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
