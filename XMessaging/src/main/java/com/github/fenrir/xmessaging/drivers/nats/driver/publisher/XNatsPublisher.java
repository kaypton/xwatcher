package com.github.fenrir.xmessaging.drivers.nats.driver.publisher;

import com.alibaba.fastjson.JSONObject;
import com.github.fenrir.xmessaging.XMessageBuilder;
import com.github.fenrir.xmessaging.XMessagingPublisher;
import com.github.fenrir.xmessaging.drivers.nats.driver.XNatsDriverBase;
import com.github.fenrir.xmessaging.drivers.nats.driver.XNatsSettings;
import com.github.fenrir.xmessaging.XMessage;
import io.nats.client.Message;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class XNatsPublisher extends XNatsDriverBase implements XMessagingPublisher {

    private XNatsPublisher(List<String> natsServerAddresses,
                           String topic,
                           XNatsSettings.PublishMode publishMode){
        super(natsServerAddresses, topic, null, publishMode);
    }

    public XMessage request(XMessage msg){
        return this.publishAndWaitReply(msg.getNatsRawMessage());
    }

    public XMessage asyncRequest(XMessage msg){
        return this.publishNotWaitReply(msg.getNatsRawMessage());
    }

    public XMessage request(String msg){
        XMessage _msg = XMessageBuilder.builder(this.getTopic())
                .setStringPayload(msg)
                .buildNatsMessage();
        return this.publishAndWaitReply(_msg.getNatsRawMessage());
    }

    public void send(XMessage msg){
        this.publishWithoutReply(msg.getNatsRawMessage());
    }

    public void send(String msg){
        XMessage _msg = XMessageBuilder.builder(this.getTopic())
                .setStringPayload(msg)
                .buildNatsMessage();
        this.publishWithoutReply(_msg.getNatsRawMessage());
    }

    public void send(JSONObject jsonMsg){
        XMessage _msg = XMessageBuilder.builder(this.getTopic())
                .setStringPayload(jsonMsg.toJSONString())
                .buildNatsMessage();
        this.publishWithoutReply(_msg.getNatsRawMessage());
    }

    static public XNatsPublisher create(List<String> natsServerAddresses,
                                        String topic,
                                        XNatsSettings.PublishMode publishMode){
        return new XNatsPublisher(natsServerAddresses, topic, publishMode);
    }
}
