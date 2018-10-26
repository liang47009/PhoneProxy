package com.yunfeng.tools.phoneproxy.listener;

import xiaofei.library.hermes.annotation.ClassId;
import xiaofei.library.hermes.annotation.MethodId;

@ClassId("SocketProxy")
public interface IProxyEventTask {

    @MethodId("isRunning")
    boolean isRunning();

    @MethodId("start")
    void start(ProxyEventListener eventListener);

    @MethodId("stop")
    void stop();
}
