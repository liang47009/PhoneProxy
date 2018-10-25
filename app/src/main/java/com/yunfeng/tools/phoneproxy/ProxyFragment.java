package com.yunfeng.tools.phoneproxy;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.yunfeng.tools.phoneproxy.util.Logger;

public class ProxyFragment extends Fragment {

    private ProxyViewModel mViewModel;

    public static ProxyFragment newInstance() {
        return new ProxyFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.proxy_fragment, container, false);
        if (null != view) {
            if (mViewModel == null) {
                mViewModel = ViewModelProviders.of(this).get(ProxyViewModel.class);
            }
            Logger.d("onCreateView: " + mViewModel);
            final TextView tv = view.findViewById(R.id.message);
            mViewModel.mutableLiveData.observe(this, new Observer<String>() {
                @Override
                public void onChanged(String data) {
                    // update ui.
                    Logger.d("onchanged: " + data);
                    tv.setText(data);
                }
            });
            tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mViewModel.doAction(v.toString());
                }
            });
        }
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(ProxyViewModel.class);
        // TODO: Use the ViewModel
        Logger.d("onActivityCreated: " + mViewModel);
    }

}
