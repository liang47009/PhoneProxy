package com.yunfeng.tools.phoneproxy.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;
import java.util.Map;

public class ProxyViewModel extends ViewModel {
    // TODO: Implement the ViewModel
//    public final MutableLiveData<String> mutableLiveData = new MutableLiveData<>();
    public final MutableLiveData<List<Map<String, Object>>> listems = new MutableLiveData<>();

    public MutableLiveData<List<Map<String, Object>>> getListItems() {
        return listems;
    }

}
