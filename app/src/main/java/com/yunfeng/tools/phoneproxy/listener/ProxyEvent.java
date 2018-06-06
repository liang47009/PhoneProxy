package com.yunfeng.tools.phoneproxy.listener;

/**
 * e
 * Created by xll on 2018/1/6.
 */

public class ProxyEvent {

    private Object data;
    private EventType eventType;

    public enum EventType {
        LOG_EVENT, DATA_EVENT, ERROR_EVENT, SERVER_START_EVENT, SERVER_STOP_EVENT
    }

    public ProxyEvent(EventType logEvent, Object msg) {
        this.eventType = logEvent;
        this.data = msg;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }


}
