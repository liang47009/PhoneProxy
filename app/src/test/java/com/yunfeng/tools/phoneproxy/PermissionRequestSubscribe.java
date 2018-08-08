package com.yunfeng.tools.phoneproxy;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

import com.yunfeng.tools.phoneproxy.util.Utils;

import java.lang.ref.WeakReference;

import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;

/**
 * permission subscribe
 * Created by xll on 2018/8/7.
 */
public class PermissionRequestSubscribe implements ObservableOnSubscribe<Boolean> {

    private WeakReference<Activity> mActivity;

    public PermissionRequestSubscribe(Activity activity) {
        mActivity = new WeakReference<>(activity);
    }

    @Override
    public void subscribe(ObservableEmitter<Boolean> emitter) throws Exception {
        Activity activity = mActivity.get();
        if (activity == null) {
            emitter.onError(new NullPointerException("Activity is Null"));
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                for (String permission : Utils.permissions) {
                    int granted = activity.checkSelfPermission(permission);
                    if (granted == PackageManager.PERMISSION_DENIED) {
                        emitter.onNext(false);
                    } else {
                        emitter.onNext(true);
                    }
                }
            }
        }
    }
}
