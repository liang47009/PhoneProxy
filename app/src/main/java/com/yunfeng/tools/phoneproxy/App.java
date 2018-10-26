package com.yunfeng.tools.phoneproxy;

import android.app.Application;
import android.support.multidex.MultiDex;

import com.yunfeng.tools.phoneproxy.http.SocketProxy;

import xiaofei.library.hermes.Hermes;

/**
 * app
 * Created by xll on 2016/5/19.
 */
public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        MultiDex.install(this);
        Hermes.init(this);
        Hermes.register(SocketProxy.class);
    }
}
