package com.yunfeng.tools.phoneproxy.listener;

import com.google.android.gms.ads.AdListener;
import com.yunfeng.tools.phoneproxy.util.Logger;

/**
 * aaa
 * Created by xll on 2018/1/8.
 */
public class AdMobListener extends AdListener {
    @Override
    public void onAdLoaded() {
        // Code to be executed when an ad finishes loading.
        Logger.d("onAdLoaded");
    }

    @Override
    public void onAdFailedToLoad(int errorCode) {
        // Code to be executed when an ad request fails.
        Logger.d("onAdFailedToLoad: " + errorCode);
    }

    @Override
    public void onAdOpened() {
        // Code to be executed when an ad opens an overlay that
        // covers the screen.
        Logger.d("onAdOpened");
    }

    @Override
    public void onAdLeftApplication() {
        // Code to be executed when the user has left the app.
        Logger.d("onAdLeftApplication");
    }

    @Override
    public void onAdClosed() {
        // Code to be executed when when the user is about to return
        // to the app after tapping on an ad.
        Logger.d("onAdClosed");
    }
}
