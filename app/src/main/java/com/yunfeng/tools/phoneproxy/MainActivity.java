package com.yunfeng.tools.phoneproxy;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.yunfeng.tools.phoneproxy.http.HttpsServer;
import com.yunfeng.tools.phoneproxy.socket.Cert;
import com.yunfeng.tools.phoneproxy.util.Utils;

import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    private TextView tv = null;

    private static final int REQUEST_PERMISIONS = 0xffffff;

    private static final String[] permissions = new String[]{Manifest.permission.INTERNET, Manifest.permission.WRITE_EXTERNAL_STORAGE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv = (TextView) findViewById(R.id.proxy_info);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (String permission : permissions) {
                int granted = checkSelfPermission(permission);
                if (granted == PackageManager.PERMISSION_DENIED) {
                    requestPermissions(permissions, REQUEST_PERMISIONS);
                }
            }
        }
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

    private transient boolean start_server = false;

    public void startProxy(View view) {
        if (start_server) {
            start_server = false;
            HttpsServer.stop();
            tv.setText(R.string.stoped);
            ((Button) view).setText(getText(R.string.start_proxy));
        } else {
            start_server = true;
            Cert.init(this);
//        new Server().startup("0.0.0.0", 8888);
            try {
//            SecureChatServer.startup("0.0.0.0", 8888);
//            HttpsMockServer.startup("0.0.0.0", 8888);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            InputStream in = MainActivity.this.getAssets().open("server.bks");
                            HttpsServer.startup(in);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            } catch (Exception e) {
                e.printStackTrace();
            }
            tv.setText(Utils.getLocalIpAddress() + ":8888");
            ((Button) view).setText(getText(R.string.stop_proxy));
        }
    }

}
