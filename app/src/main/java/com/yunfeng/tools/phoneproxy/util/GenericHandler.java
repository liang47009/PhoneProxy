package com.yunfeng.tools.phoneproxy.util;

import android.os.Handler;
import android.os.Message;

import java.lang.ref.WeakReference;

/**
 * handler
 */
public class GenericHandler extends Handler {

    private WeakReference<MessageCallback> weakReference;

    public WeakReference<MessageCallback> getWeakReference() {
        return weakReference;
    }

    public void setWeakReference(WeakReference<MessageCallback> weakReference) {
        this.weakReference = weakReference;
    }

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        if (weakReference != null && weakReference.get() != null) {
            weakReference.get().handleMessage(msg);
        } else {
            Logger.e("GenericHandler reference is null");
        }

    }

    public interface MessageCallback {
        void handleMessage(Message msg);
    }
}
