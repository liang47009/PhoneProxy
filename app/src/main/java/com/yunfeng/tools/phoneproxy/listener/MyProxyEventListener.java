package com.yunfeng.tools.phoneproxy.listener;

import android.app.Activity;
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
                if (event.getEventType() == ProxyEvent.EventType.LOG_EVENT) {
                    EditText view = (EditText) mActivity.findViewById(R.id.log_editText);
                    view.append(event.getData().toString());
                } else if (event.getEventType() == ProxyEvent.EventType.DATA_EVENT) {
                    DataEventObject data = (DataEventObject) event.getData();
                    totalUpStream += data.getUpStream();
                    totalDownStream += data.getDownStream();
                    TextView view = (TextView) mActivity.findViewById(R.id.data_textView);
                    view.setText(String.format(Locale.CHINA, "TotalUpStream: %d \r\nTotalDownStream: %d", totalUpStream, totalDownStream));
                } else if (event.getEventType() == ProxyEvent.EventType.ERROR_EVENT) {
                    ErrorEventObject data = (ErrorEventObject) event.getData();
                    Toast.makeText(mActivity, data.getErrorMsg() + ", " + data.getThrowable().getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
