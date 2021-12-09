package com.github.fenrir.xsniffer.listener;

import com.github.fenrir.xcommon.message.MessageFactory;
import com.github.fenrir.xcommon.message.stream.BaseStream;
import com.github.fenrir.xmessaging.*;
import com.github.fenrir.xsniffer.exporter.Exporter;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StreamListener implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger("StreamListener");

    @Getter @Setter private String streamName;
    @Getter @Setter private String streamTopicName;
    @Getter @Setter private Exporter exporter;

    private static class SnifferStreamListenerCallback implements MessageProcessCallBack {
        @Getter @Setter private Exporter exporter = null;
        public SnifferStreamListenerCallback(Exporter exporter){
            this.setExporter(exporter);
        }
        @Override
        public void processMessage(XMessage msg) {
            logger.info(msg.getStringData());
            if(this.getExporter() != null)
                this.getExporter().export(msg);
        }
    }

    public StreamListener(String streamName, Exporter exporter){
        this.setStreamName(streamName);
        this.setExporter(exporter);
        this.setStreamTopicName("stream." + streamName);
    }

    @Override
    public void run(){
        XMessagingListener listener = XMessaging.createListener(this.getStreamTopicName(),
                new SnifferStreamListenerCallback(this.getExporter()));
        logger.info("listening ...");
        listener.join();
    }
}
