package com.github.fenrir.xmessaging.drivers.nats.driver;

import com.github.fenrir.xmessaging.MessageProcessCallBack;
import com.github.fenrir.xmessaging.XMessage;
import io.nats.client.*;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class XNatsDriverBase {
    static private final Logger LOGGER = LoggerFactory.getLogger("XNatsDriverBase");
    /**
     * topic to subscribe
     */
    @Getter @Setter private String topic = null;

    /**
     * nats server connection object
     */
    private Connection natsConnection;

    /**
     * nats consume mode
     */
    @Getter @Setter private XNatsSettings.ConsumeMode consumeMode = null;

    /**
     * nats publish mode
     */
    @Getter @Setter private XNatsSettings.PublishMode publishMode = null;

    /**
     * receiving thread object
     */
    @Getter @Setter private Thread receivingThread = null;

    /**
     * receiving thread id
     */
    @Getter @Setter private Long receivingThreadId = (long) -1;

    protected XNatsDriverBase(List<String> natsServerAddresses,
                              String topic,
                              XNatsSettings.ConsumeMode consumeMode,
                              XNatsSettings.PublishMode publishMode){

        this.setTopic(topic);
        this.setConsumeMode(consumeMode);
        this.setPublishMode(publishMode);

        if(natsServerAddresses == null){
            LOGGER.error("nats address list is null");
            System.exit(-1);
        }

        Options.Builder natsConnectionOptionsBuilder = new Options.Builder();
        for(String natsServerAddress : natsServerAddresses){
            natsConnectionOptionsBuilder.server(natsServerAddress);
        }

        Options natsConnectionOptions = natsConnectionOptionsBuilder.build();

        try {
            this.natsConnection = Nats.connect(natsConnectionOptions);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            LOGGER.error(e.getMessage());
            this.natsConnection = null;
            System.exit(-1);
        }
    }

    protected void publishWithReply(){

    }

    protected void publishWithoutReply(Message msg){
        this.natsConnection.publish(msg);
    }

    protected XMessage publishAndWaitReply(Message msg){
        try {
            Message _msg = this.natsConnection.request(msg).get();
            return XMessage.create(_msg);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            LOGGER.error(e.getMessage());
            return null;
        }
    }

    protected XMessage publishNotWaitReply(Message msg){
        CompletableFuture<Message> futureMessage = this.natsConnection.request(msg);
        return new XMessage(futureMessage);
    }

    protected void runReceiver(BlockingQueue<XMessage> receiveQueue,
                               MessageProcessCallBack messageProcessCallBack){
        Dispatcher dispatcher = this.natsConnection.createDispatcher(
                        new NatsDispatcherHandler(
                                receiveQueue,
                                messageProcessCallBack,
                                this.getConsumeMode()))
                .subscribe(this.getTopic());
    }

    private static class NatsDispatcherHandler implements MessageHandler{
        private final MessageProcessCallBack messageProcessCallBack;
        private final BlockingQueue<XMessage> queue;
        private final XNatsSettings.ConsumeMode consumeMode;

        public NatsDispatcherHandler(BlockingQueue<XMessage> queue,
                                     MessageProcessCallBack messageProcessCallBack,
                                     XNatsSettings.ConsumeMode consumeMode){
            this.messageProcessCallBack = messageProcessCallBack;
            this.queue = queue;
            this.consumeMode = consumeMode;
        }
        @Override
        public void onMessage(Message msg) throws InterruptedException {
            XMessage message = XMessage.create(msg);
            if(this.consumeMode == XNatsSettings.ConsumeMode.ASYNC && this.messageProcessCallBack != null){
                this.messageProcessCallBack.processMessage(message);
            }else if(this.consumeMode == XNatsSettings.ConsumeMode.SYNC && this.queue != null){
                this.queue.put(message);
            }
        }
    }
}
