package com.github.fenrir.xsniffer.listener;

import com.github.fenrir.xmessaging.XMessage;
import com.github.fenrir.xmessaging.XMessaging;
import com.github.fenrir.xmessaging.XMessagingListener;
import com.github.fenrir.xmessaging.MessageProcessCallBack;
import com.github.fenrir.xsniffer.exporter.Exporter;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventListener implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger("EventListener");
    
    @Getter @Setter private String eventName;
    @Getter @Setter private String eventTopicName;
    @Getter @Setter private Exporter exporter;

    private static class SnifferEventListenerCallback implements MessageProcessCallBack {
        @Getter @Setter private Exporter exporter = null;
        public SnifferEventListenerCallback(Exporter exporter){
            this.setExporter(exporter);
        }
        @Override
        public void processMessage(XMessage data) {
            logger.info(data.getStringData());
            if(this.getExporter() != null)
                this.getExporter().export(data);
        }
    }

    public EventListener(String eventName, Exporter exporter){
        this.setEventName(eventName);
        this.setExporter(exporter);
        this.setEventTopicName("event." + eventName);
    }

    @Override
    public void run(){
        XMessagingListener listener =
                XMessaging.createListener(this.getEventTopicName(),
                        new SnifferEventListenerCallback(this.getExporter()));
        logger.info(Thread.currentThread().getName() + "listening ... ");
        listener.block();
    }
}
