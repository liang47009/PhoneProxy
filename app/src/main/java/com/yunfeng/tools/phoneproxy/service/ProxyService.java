//package com.yunfeng.tools.phoneproxy.service;
//
//import android.app.Service;
//import android.content.Intent;
//import android.os.IBinder;
//
//import com.yunfeng.tools.phoneproxy.R;
//import com.yunfeng.tools.phoneproxy.http.SocketProxy;
//import com.yunfeng.tools.phoneproxy.listener.ProxyEvent;
//import com.yunfeng.tools.phoneproxy.util.Logger;
//import com.yunfeng.tools.phoneproxy.util.NotificationUtils;
//
//public class ProxyService extends Service {
//    private final SocketProxy socketProxy = new SocketProxy("8888");
//
////    private final IProxyAidlInterface.Stub mBinder = new IProxyAidlInterface.Stub() {
////
////        @Override
////        public void onEvent(IProxyEvent event) throws RemoteException {
////
////        }
////    };
////
////    @Override
////    public IBinder onBind(Intent intent) {
////        return mBinder;
////    }
//
//    @Override
//    public IBinder onBind(Intent intent) {
//        return new ProxyBinder();
//    }
//
//    public class ProxyBinder extends android.os.Binder {
//
//        public ProxyService getService() {
//            return ProxyService.this;
//        }
//    }
//
//    @Override
//    public void onCreate() {
//        Logger.e("sdd onCreate");
//        super.onCreate();
//        NotificationUtils.showNotifyOnlyText(this, R.mipmap.ic_launcher, R.mipmap.icon);
//    }
//
//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//        Logger.e("sdd onStartCommand: " + flags + ", " + startId);
//        return super.onStartCommand(intent, flags, startId);
//    }
//
//    @Override
//    public void onDestroy() {
//        socketProxy.onDestory(this);
//        super.onDestroy();
//        Logger.e("sdd onDestroy");
//    }
//
//    private CallBack callback = null;
//
//    public void setCallback(CallBack callback) {
//        this.callback = callback;
//    }
//
//    public CallBack getCallback() {
//        return callback;
//    }
//
//    public interface CallBack {
//        void onDataChange(ProxyEvent event);
//    }
//}
