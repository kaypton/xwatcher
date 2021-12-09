package com.github.fenrir.xmessaging;

public interface XMessagingListener {
    XMessage receiveMessage(Integer durationMs);
    void join();
}
