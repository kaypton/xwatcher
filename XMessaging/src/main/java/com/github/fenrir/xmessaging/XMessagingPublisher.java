package com.github.fenrir.xmessaging;

import com.alibaba.fastjson.JSONObject;

public interface XMessagingPublisher {
    XMessage request(XMessage msg);
    XMessage asyncRequest(XMessage msg);
    XMessage request(String msg);
    void send(XMessage msg);
    void send(String msg);
    void send(JSONObject jsonMsg);
}
