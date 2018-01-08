package com.yunfeng.tools.phoneproxy.listener;

/**
 * deo
 * Created by xll on 2018/1/6.
 */
public class DataEventObject {
    private long upStream;
    private long downStream;

    public DataEventObject(long totalUpload, long totalDownload) {
        this.upStream = totalUpload;
        this.downStream = totalDownload;
    }

    public long getDownStream() {
        return downStream;
    }

    public void setDownStream(long downStream) {
        this.downStream = downStream;
    }

    public long getUpStream() {
        return upStream;
    }

    public void setUpStream(long upStream) {
        this.upStream = upStream;
    }
}
