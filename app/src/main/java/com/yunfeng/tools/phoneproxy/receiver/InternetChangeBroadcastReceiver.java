package com.yunfeng.tools.phoneproxy.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.widget.Toast;

import com.yunfeng.tools.phoneproxy.util.Utils;

/**
 * Receiver
 * Created by xll on 2018/8/8.
 */
public class InternetChangeBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Utils.internetChange(context, intent);
//        //检测API是不是小于23，因为到了API23之后getNetworkInfo(int networkType)方法被弃用
//        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {
//            //获得ConnectivityManager对象
//            ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
//            if (null != connMgr) {
//                //获取ConnectivityManager对象对应的NetworkInfo对象
//                //获取WIFI连接的信息
//                NetworkInfo wifiNetworkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
//                //获取移动数据连接的信息
//                NetworkInfo dataNetworkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
//                if (wifiNetworkInfo.isConnected() && dataNetworkInfo.isConnected()) {
//                    Toast.makeText(context, "WIFI已连接,移动数据已连接", Toast.LENGTH_SHORT).show();
//                } else if (wifiNetworkInfo.isConnected() && !dataNetworkInfo.isConnected()) {
//                    Toast.makeText(context, "WIFI已连接,移动数据已断开", Toast.LENGTH_SHORT).show();
//                } else if (!wifiNetworkInfo.isConnected() && dataNetworkInfo.isConnected()) {
//                    Toast.makeText(context, "WIFI已断开,移动数据已连接", Toast.LENGTH_SHORT).show();
//                } else {
//                    Toast.makeText(context, "WIFI已断开,移动数据已断开", Toast.LENGTH_SHORT).show();
//                }
//            }
//        } else {
//            //获得ConnectivityManager对象
//            ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
//            if (null != connMgr) {
//                //获取所有网络连接的信息
//                Network[] networks = connMgr.getAllNetworks();
//                //用于存放网络连接信息
//                StringBuilder sb = new StringBuilder();
//                //通过循环将网络信息逐个取出来
//                for (Network network : networks) {
//                    //获取ConnectivityManager对象对应的NetworkInfo对象
//                    NetworkInfo networkInfo = connMgr.getNetworkInfo(network);
//                    sb.append(networkInfo.getTypeName()).append(" connect is ").append(networkInfo.isConnected());
//                }
//                Toast.makeText(context, sb.toString(), Toast.LENGTH_SHORT).show();
//            }
//        }
    }
}
