package com.yunfeng.tools.phoneproxy;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.TextView;

import com.yunfeng.tools.phoneproxy.http.HttpsServer;
import com.yunfeng.tools.phoneproxy.socket.Cert;

import org.bouncycastle.util.encoders.Base64Encoder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.util.Enumeration;

public class MainActivity extends AppCompatActivity {

    private TextView tv = null;

    private static final int REQUEST_PERMISIONS = 0xffffff;

    private static final String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        showWelDialog(this);

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

//    private void showWelDialog(final Activity activity) {
//        final Dialog channleWelDialog = new Dialog(activity, R.style.Channel_CustomDialog);
//        channleWelDialog.setContentView(R.layout.channel_welcome);
//        final ImageView view = (ImageView) channleWelDialog.findViewById(R.id.image_view);
//        final Handler mHandler = new Handler();
//        Animation.AnimationListener firstAnimationListener = new Animation.AnimationListener() {
//            @Override
//            public void onAnimationStart(Animation arg0) {
//            }
//
//            @Override
//            public void onAnimationRepeat(Animation arg0) {
//            }
//
//            @Override
//            public void onAnimationEnd(Animation arg0) {
//                view.setBackgroundDrawable(null);
//                mHandler.postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        view.setBackgroundResource(R.drawable.second);
//                        Animation.AnimationListener thirdAnimationListener = new Animation.AnimationListener() {
//                            @Override
//                            public void onAnimationStart(Animation arg0) {
//                            }
//
//                            @Override
//                            public void onAnimationRepeat(Animation arg0) {
//                            }
//
//                            @Override
//                            public void onAnimationEnd(Animation arg0) {
//                                mHandler.postDelayed(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        channleWelDialog.dismiss();
//                                        view.setBackgroundDrawable(null);
//                                        System.gc();
//                                    }
//                                }, 3000);
//                            }
//                        };
//                        splashAnimation(view, thirdAnimationListener);
//                    }
//                }, 3000);
//            }
//        };
//        splashAnimation(view, firstAnimationListener);
//        Drawable drawable = activity.getResources().getDrawable(bg_id);
//        view.setBackgroundDrawable(drawable);
//        channleWelDialog.show();
//        final Timer tTimer = new Timer();
//        tTimer.schedule(new TimerTask() {
//            @Override
//            public void run() {
//                activity.runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        view.setBackgroundDrawable(null);
//                        view.setBackgroundResource(R.drawable.second);
//                        tTimer.schedule(new TimerTask() {
//                            @Override
//                            public void run() {
//                                activity.runOnUiThread(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        view.setBackgroundDrawable(null);
//                                        channleWelDialog.dismiss();
//                                    }
//                                });
//                                tTimer.cancel();
//                            }
//                        }, 3000);
//                    }
//                });
//            }
//        }, 3000);
//
//    }

    private void splashAnimation(View view, Animation.AnimationListener animationListener) {
        ScaleAnimation scaleAnimation = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        scaleAnimation.setDuration(600);
        scaleAnimation.setAnimationListener(animationListener);
        view.startAnimation(scaleAnimation);
    }

    @Override
    @TargetApi(Build.VERSION_CODES.M)
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISIONS:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("PERMISSION_GRANTED is true!");
                    export("BKS", "clientkey", "a123456".toCharArray(), "/sdcard/exported.crt");
                } else {
                    requestPermissions(permissions, REQUEST_PERMISIONS);
                }
                break;
        }
    }

    public KeyPair getPrivateKey(KeyStore keystore, String alias, char[] password) {
        try {
            Key key = keystore.getKey(alias, password);
            if (key instanceof PrivateKey) {
                Certificate cert = keystore.getCertificate(alias);
                PublicKey publicKey = cert.getPublicKey();
                return new KeyPair(publicKey, (PrivateKey) key);
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    public void export(String type, String alias, char[] password,
                       String outFilePath) {
        try {
            KeyStore keystore = KeyStore.getInstance(type);
            InputStream in = this.getAssets().open("bksmy.keystore");
            keystore.load(in, password);
            KeyPair keyPair = getPrivateKey(keystore, alias, password);
            PrivateKey privateKey = keyPair.getPrivate();
            FileOutputStream writer = new FileOutputStream(
                    new File(outFilePath));
            Base64Encoder encoder = new Base64Encoder();
            int encoded = encoder.encode(privateKey.getEncoded(), 0,
                    privateKey.getEncoded().length, writer);
            System.out.println("size:" + encoded);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private transient boolean start_server = false;

    public void startProxy(View view) {
        if (start_server) {
            start_server = false;
            HttpsServer.stop();
            tv.setText(getText(R.string.start_proxy));
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
            tv.setText(getLocalIpAddress() + ":8888");
        }
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
