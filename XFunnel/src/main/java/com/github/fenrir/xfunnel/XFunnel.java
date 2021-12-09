package com.github.fenrir.xfunnel;

import com.github.fenrir.xcommon.message.MessageFactory;
import com.github.fenrir.xcommon.message.event.BaseEvent;
import com.github.fenrir.xcommon.message.stream.BaseStream;
import com.github.fenrir.xmessaging.XMessaging;
import com.github.fenrir.xmessaging.XMessagingListener;
import com.github.fenrir.xmessaging.MessageProcessCallBack;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public class XFunnel {
    @Getter private static final Map<XMessagingListener, BaseStream> streamListeningMap = new HashMap<>();
    @Getter private static final Map<XMessagingListener, BaseEvent> eventListeningMap = new HashMap<>();

    public static XFunnel getInstance(String xmessagingInitJsonString){
        return new XFunnel(xmessagingInitJsonString);
    }

    private static MessageFactory messageFactory;

    private XFunnel(String config){
        XMessaging.init(config);
        messageFactory = MessageFactory.create(new String[]{"com.github.fenrir.xcommon.message.event",
            "com.github.fenrir.xcommon.message.stream"});
    }

    public void listenStream(String streamName, MessageProcessCallBack callBack){
        BaseStream stream = messageFactory.getStream(streamName);
        if(stream == null) return;

        streamListeningMap.put(XMessaging.createListener(stream.getStreamTopicName(), callBack), stream);
    }

    public void listenEvent(String eventName, MessageProcessCallBack callBack){
        BaseEvent event = messageFactory.getEvent(eventName);
        if(event == null) return;

        eventListeningMap.put(XMessaging.createListener(event.getEventTopicName(), callBack), event);
    }
}
