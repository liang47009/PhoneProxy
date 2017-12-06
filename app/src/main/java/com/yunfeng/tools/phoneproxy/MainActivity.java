package com.yunfeng.tools.phoneproxy;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.yunfeng.tools.phoneproxy.socket.Cert;
import com.yunfeng.tools.phoneproxy.socket.Server;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class MainActivity extends AppCompatActivity {

    private TextView tv = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv = (TextView) findViewById(R.id.proxy_info);
    }

    public void startProxy(View view) {
        Cert.init(this);
        new Server().startup("0.0.0.0", 8888);
        tv.setText(getLocalIpAddress() + ":8888");
    }

    public String getLocalIpAddress() {
        try {
            Enumeration<NetworkInterface> ens = NetworkInterface.getNetworkInterfaces();
            while (ens.hasMoreElements()) {
                NetworkInterface networkInterface = ens.nextElement();
                Enumeration<InetAddress> ias = networkInterface.getInetAddresses();
                while (ias.hasMoreElements()) {
                    InetAddress inetAddress = ias.nextElement();
                    if (!inetAddress.isLoopbackAddress() && (inetAddress instanceof Inet4Address)) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return null;
    }

}
