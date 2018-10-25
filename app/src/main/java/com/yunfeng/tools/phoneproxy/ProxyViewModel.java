package com.yunfeng.tools.phoneproxy;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import com.yunfeng.tools.phoneproxy.util.Logger;

public class ProxyViewModel extends ViewModel {
    // TODO: Implement the ViewModel
    public final MutableLiveData<String> mutableLiveData = new MutableLiveData<>();

    void doAction(String value) {
        // depending on the action, do necessary business logic calls and update the liveData.
        Logger.d("doAction: " + this);
        mutableLiveData.setValue(value);
    }
}
