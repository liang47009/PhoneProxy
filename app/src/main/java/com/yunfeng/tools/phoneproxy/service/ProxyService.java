package com.yunfeng.tools.phoneproxy.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.yunfeng.tools.phoneproxy.http.SocketProxy;
import com.yunfeng.tools.phoneproxy.listener.ProxyEvent;
import com.yunfeng.tools.phoneproxy.listener.ProxyEventListener;
import com.yunfeng.tools.phoneproxy.util.Logger;

public class ProxyService extends Service {
    private final SocketProxy socketProxy = new SocketProxy();

    @Override
    public IBinder onBind(Intent intent) {
        return new ProxyBinder();
    }

    public class ProxyBinder extends android.os.Binder {

        public ProxyService getService() {
            return ProxyService.this;
        }
    }

    @Override
    public void onCreate() {
        Logger.e("sdd onCreate");
        super.onCreate();
        socketProxy.startup("8888", new ProxyEventListener() {
            @Override
            public void onEvent(ProxyEvent event) {
                if (ProxyService.this.callback != null) {
                    ProxyService.this.callback.onDataChange(event);
                }
            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Logger.e("sdd onStartCommand: " + flags + ", " + startId);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        socketProxy.onDestory(this);
        super.onDestroy();
        Logger.e("sdd onDestroy");
    }

    private CallBack callback = null;

    public void setCallback(CallBack callback) {
        this.callback = callback;
    }

    public CallBack getCallback() {
        return callback;
    }

    public interface CallBack {
        void onDataChange(ProxyEvent event);
    }
}
