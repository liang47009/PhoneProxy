package com.yunfeng.tools.phoneproxy.listener;

/**
 * error
 * Created by xll on 2018/1/8.
 */
public class ErrorEventObject {

    private String errorMsg;
    private Throwable throwable;

    public ErrorEventObject(String errorMsg, Throwable throwable) {
        this.errorMsg = errorMsg;
        this.throwable = throwable;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    @Override
    public String toString() {
        return "ErrorEventObject{" +
                "errorMsg='" + errorMsg + '\'' +
                ", throwable=" + throwable +
                '}';
    }
}
