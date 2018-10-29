package com.yunfeng.tools.phoneproxy;

import android.app.ActivityManager;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
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
import com.yunfeng.tools.phoneproxy.listener.ErrorEventObject;
import com.yunfeng.tools.phoneproxy.listener.IProxyEventTask;
import com.yunfeng.tools.phoneproxy.listener.ProxyEvent;
import com.yunfeng.tools.phoneproxy.listener.ProxyEventListener;
import com.yunfeng.tools.phoneproxy.util.Logger;
import com.yunfeng.tools.phoneproxy.util.Utils;
import com.yunfeng.tools.phoneproxy.viewmodel.ProxyViewModel;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import xiaofei.library.hermes.Hermes;
import xiaofei.library.hermes.HermesListener;
import xiaofei.library.hermes.HermesService;

public class ProxyFragment extends Fragment implements View.OnClickListener, HermesListener {
    private static final int PROXY_EVENT = 1001;
    public final List<Map<String, Object>> listems = new ArrayList<Map<String, Object>>();

    private long totalUpStream;
    private long totalDownStream;
    private static IProxyEventTask proxyTask;
    private ProxyViewModel mViewModel;
    private NetworkSimpleAdapter simpleAdapter = null;
    private View contentView;
    private Handler handler;
    private WeakReference<Context> mContext;

    @Override
    public void onHermesConnected(Class<? extends HermesService> service) {
        if (null == proxyTask) {
            proxyTask = Hermes.newInstance(IProxyEventTask.class, "8888");
        }
    }

    @Override
    public void onHermesDisconnected(Class<? extends HermesService> service) {

    }

    private static class ProxyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case PROXY_EVENT:
                    break;
            }
        }
    }

    private static final ProxyFragment pf = new ProxyFragment();

    public static ProxyFragment newInstance() {
        return pf;
    }


    class ProxyEventList implements ProxyEventListener {
        @Override
        public void onEvent(final ProxyEvent event) {
            if (null != handler) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        ProxyFragment.this.handlerProxyEvent(event);
                    }
                });
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.start_proxy) {
            if (proxyTask != null) {
                proxyTask.start(new ProxyEventList());
            }
        } else if (v.getId() == R.id.stop_proxy) {
//            if (isServiceRunning(v.getContext(), ProxyService.class.getName())) {
//                v.getContext().stopService(new Intent(v.getContext(), ProxyService.class));
//                NotificationUtils.clearNotify(v.getContext());
//            }
            if (proxyTask != null && proxyTask.isRunning()) {
                proxyTask.stop();
            }
            this.contentView.findViewById(R.id.start_proxy).setEnabled(true);
        } else if (v.getId() == R.id.clearLog) {
            EditText logEditTextView = (EditText) contentView.findViewById(R.id.log_editText);
            logEditTextView.setText("");
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = new WeakReference<>(context.getApplicationContext());
        if (null != mContext.get()) {
            Hermes.connect(mContext.get(), HermesService.HermesService0.class);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        contentView = inflater.inflate(R.layout.proxy_fragment, container, false);
        if (null != contentView) {
            handler = new Handler();

            if (null != proxyTask && proxyTask.isRunning()) {
                View btn_start_proxy = contentView.findViewById(R.id.start_proxy);
                btn_start_proxy.setEnabled(false);
            }

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
    public void onDetach() {
//        Context context = this.contentView.getContext();
//        if (isServiceRunning(context, ProxyService.class.getName())) {
//            context.unbindService(this);
//            NotificationUtils.clearNotify(context);
//        }
        Context context = mContext.get();
        if (null != context) {
                Hermes.disconnect(context);
        }
        super.onDetach();
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

    private void handlerProxyEvent(ProxyEvent event) {
        if (null != contentView && contentView.getVisibility() == View.VISIBLE) {
            switch (event.getEventType()) {
                case LOG_EVENT: {
                    EditText view = this.contentView.findViewById(R.id.log_editText);
                    view.append(event.getData().toString());
                    break;
                }
                case DATA_EVENT: {
                    Object obj = event.getData();
                    if (obj instanceof ErrorEventObject) {
                        ErrorEventObject data = (ErrorEventObject) obj;
                        Toast.makeText(this.contentView.getContext(), "error, " + data.getThrowable().getMessage(), Toast.LENGTH_LONG).show();
                    } else if (obj instanceof Map) {
                        Map map = (Map) obj;
                        totalUpStream += Float.valueOf(String.valueOf(map.get("upStream")));
                        totalDownStream += Float.valueOf(String.valueOf(map.get("downStream")));
                        TextView view = this.contentView.findViewById(R.id.data_textView);
                        view.setText(String.format(Locale.CHINA, "TotalUpStream: %d \r\nTotalDownStream: %d", totalUpStream, totalDownStream));
                    }
                    break;
                }
                case ERROR_EVENT: {
                    Object obj = event.getData();
                    if (obj instanceof ErrorEventObject) {
                        ErrorEventObject data = (ErrorEventObject) event.getData();
                        Toast.makeText(this.contentView.getContext(), "error, " + data.getThrowable().getMessage(), Toast.LENGTH_LONG).show();
                    } else if (obj instanceof Map) {
                        Map map = (Map) obj;
                        StringBuilder stringBuilder = new StringBuilder("error: ");
                        for (Object key : map.keySet()) {
                            Object value = map.get(key);
                            stringBuilder.append(value);
                        }
                        Toast.makeText(this.contentView.getContext(), stringBuilder.toString(), Toast.LENGTH_LONG).show();
                    }
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

}
