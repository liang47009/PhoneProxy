package com.yunfeng.tools.phoneproxy.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * socket service
 * Created by xll on 2018/8/8.
 */
public class DataStreamService extends Service {

    private static final int MSG_FROM_CLIENT_HELLO = 1;

    private Messenger message = new Messenger(new IncomingHandler());

    private static class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_FROM_CLIENT_HELLO:
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return message.getBinder();
    }

    @Override
    public void onCreate() {
        Log.d("app", "sdd onCreate");
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("app", "sdd onStartCommand: " + flags + ", " + startId);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("app", "sdd onDestroy");
    }

}
