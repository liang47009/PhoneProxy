package com.yunfeng.tools.phoneproxy.listener;

import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.yunfeng.tools.phoneproxy.R;

import java.util.Locale;

/**
 * aa
 * Created by xll on 2018/1/8.
 */
public class MyProxyEventListener implements ProxyEventListener {

    private Activity mActivity;
    private long totalUpStream;
    private long totalDownStream;

    public MyProxyEventListener(Activity activity) {
        this.mActivity = activity;
    }

    @Override
    public void onEvent(final ProxyEvent event) {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch (event.getEventType()) {
                    case LOG_EVENT: {
                        EditText view = mActivity.findViewById(R.id.log_editText);
                        view.append(event.getData().toString());
                        break;
                    }
                    case DATA_EVENT: {
                        DataEventObject data = (DataEventObject) event.getData();
                        totalUpStream += data.getUpStream();
                        totalDownStream += data.getDownStream();
                        TextView view = mActivity.findViewById(R.id.data_textView);
                        view.setText(String.format(Locale.CHINA, "TotalUpStream: %d \r\nTotalDownStream: %d", totalUpStream, totalDownStream));
                        break;
                    }
                    case ERROR_EVENT: {
                        ErrorEventObject data = (ErrorEventObject) event.getData();
                        Toast.makeText(mActivity, data.getErrorMsg() + ", " + data.getThrowable().getMessage(), Toast.LENGTH_LONG).show();
                        break;
                    }
                    case SERVER_START_EVENT: {
                        View view = mActivity.findViewById(R.id.start_proxy);
                        view.setEnabled(false);
                        break;
                    }
                    case SERVER_STOP_EVENT: {
                        Button view = mActivity.findViewById(R.id.start_proxy);
                        view.setText(R.string.start_proxy);
                        view.setEnabled(true);
                        break;
                    }
                }
            }
        });
    }
}
