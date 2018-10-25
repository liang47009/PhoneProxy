package com.yunfeng.tools.phoneproxy;

import android.app.ActivityManager;
import android.app.Service;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.yunfeng.tools.phoneproxy.adapter.NetworkSimpleAdapter;
import com.yunfeng.tools.phoneproxy.listener.AdMobListener;
import com.yunfeng.tools.phoneproxy.listener.DataEventObject;
import com.yunfeng.tools.phoneproxy.listener.ErrorEventObject;
import com.yunfeng.tools.phoneproxy.listener.ProxyEvent;
import com.yunfeng.tools.phoneproxy.service.ProxyService;
import com.yunfeng.tools.phoneproxy.util.Logger;
import com.yunfeng.tools.phoneproxy.util.Utils;
import com.yunfeng.tools.phoneproxy.viewmodel.ProxyViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ProxyFragment extends Fragment implements View.OnClickListener, ServiceConnection {
    private static final int PROXY_EVENT = 1001;
    public final List<Map<String, Object>> listems = new ArrayList<Map<String, Object>>();

    private long totalUpStream;
    private long totalDownStream;

    private ProxyViewModel mViewModel;
    private NetworkSimpleAdapter simpleAdapter = null;
    private View contentView;
    private Handler handler;
    private ProxyService.ProxyBinder mProxyService;

    public static ProxyFragment newInstance() {
        return new ProxyFragment();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.start_proxy) {
            v.getContext().bindService(new Intent(v.getContext(), ProxyService.class), this, Context.BIND_AUTO_CREATE);
            v.setEnabled(false);
        } else if (v.getId() == R.id.stop_proxy) {
            v.getContext().unbindService(this);
            this.contentView.findViewById(R.id.start_proxy).setEnabled(true);
        } else if (v.getId() == R.id.clearLog) {
            EditText logEditTextView = (EditText) contentView.findViewById(R.id.log_editText);
            logEditTextView.setText("");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        contentView = inflater.inflate(R.layout.proxy_fragment, container, false);
        if (null != contentView) {
            handler = new Handler();
            AdView adView = (AdView) contentView.findViewById(R.id.adView);
            adView.setAdListener(new AdMobListener());
            AdRequest adRequest = new AdRequest.Builder().build();
            adView.loadAd(adRequest);
            simpleAdapter = new NetworkSimpleAdapter(this.getContext(), listems, R.layout.network_list, new String[]{"name"}, new int[]{R.id.tv_ips});
            ListView listView = (ListView) contentView.findViewById(R.id.list_ips);
            listView.setAdapter(simpleAdapter);
            listView.setItemsCanFocus(false);
            listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    ClipboardManager clipboardManager = (ClipboardManager) view.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                    if (null != clipboardManager) {
                        String ip = ((TextView) view).getText().toString();
                        clipboardManager.setPrimaryClip(ClipData.newPlainText("text", ip));
                        Toast.makeText(view.getContext(), R.string.copy_to_clipboard, Toast.LENGTH_LONG).show();
                    }
                    return true;
                }
            });
            listView.setVisibility(View.VISIBLE);
            if (isServiceRunning(contentView.getContext(), ProxyService.class.getName())) {
                View btn_start_proxy = contentView.findViewById(R.id.start_proxy);
                btn_start_proxy.setEnabled(false);
            }
            contentView.findViewById(R.id.start_proxy).setOnClickListener(this);
            contentView.findViewById(R.id.stop_proxy).setOnClickListener(this);
            contentView.findViewById(R.id.clearLog).setOnClickListener(this);
            if (mViewModel == null) {
                mViewModel = ViewModelProviders.of(this).get(ProxyViewModel.class);
            }
            if (!mViewModel.getListItems().hasObservers()) {
                mViewModel.getListItems().observe(this, new Observer<List<Map<String, Object>>>() {
                    @Override
                    public void onChanged(@Nullable List<Map<String, Object>> maps) {
                        listems.clear();
                        listems.addAll(maps);
                        simpleAdapter.notifyDataSetChanged();
                        Logger.e("onChanged: " + maps);
                    }
                });
            }
            Utils.updateViewModel(this);
        }
        return contentView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(ProxyViewModel.class);
        // TODO: Use the ViewModel
        Logger.d("onActivityCreated: " + mViewModel);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /*
     * 判断服务是否启动,context上下文对象 ，className服务的name
     */
    public static boolean isServiceRunning(Context mContext, String className) {
        boolean isRunning = false;
        ActivityManager activityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> serviceList = null;
        if (activityManager != null) {
            serviceList = activityManager.getRunningServices(100);

            if (!(serviceList.size() > 0)) {
                return false;
            }
            for (ActivityManager.RunningServiceInfo info : serviceList) {
                String temp = info.service.getClassName();
                Logger.e(temp);
                if (temp.contains(className)) {
                    isRunning = true;
                    break;
                }
            }
        }
        return isRunning;
    }

    public void setNetworkInterface(final List<Map<String, Object>> maps) {
        if (null != handler) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    mViewModel.getListItems().setValue(maps);
                }
            });
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        mProxyService = (ProxyService.ProxyBinder) service;
        mProxyService.getService().setCallback(new ProxyService.CallBack() {
            @Override
            public void onDataChange(final ProxyEvent event) {
                if (null != handler) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            ProxyFragment.this.handlerProxyEvent(event);
                        }
                    });
                }
            }
        });
    }

    private void handlerProxyEvent(ProxyEvent event) {
        if (null != contentView && contentView.getVisibility() == View.VISIBLE) {
            switch (event.getEventType()) {
                case LOG_EVENT: {
                    EditText view = this.contentView.findViewById(R.id.log_editText);
                    view.append(event.getData().toString());
                    break;
                }
                case DATA_EVENT: {
                    DataEventObject data = (DataEventObject) event.getData();
                    totalUpStream += data.getUpStream();
                    totalDownStream += data.getDownStream();
                    TextView view = this.contentView.findViewById(R.id.data_textView);
                    view.setText(String.format(Locale.CHINA, "TotalUpStream: %d \r\nTotalDownStream: %d", totalUpStream, totalDownStream));
                    break;
                }
                case ERROR_EVENT: {
                    ErrorEventObject data = (ErrorEventObject) event.getData();
                    Toast.makeText(this.contentView.getContext(), data.getErrorMsg() + ", " + data.getThrowable().getMessage(), Toast.LENGTH_LONG).show();
                    break;
                }
                case SERVER_START_EVENT: {
                    View view = this.contentView.findViewById(R.id.start_proxy);
                    view.setEnabled(false);
                    break;
                }
                case SERVER_STOP_EVENT: {
                    Button view = this.contentView.findViewById(R.id.start_proxy);
                    view.setText(R.string.start_proxy);
                    view.setEnabled(true);
                    break;
                }
            }
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }

    @Override
    public void onBindingDied(ComponentName name) {

    }
}
