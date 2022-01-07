package com.github.fenrir.xmessaging.drivers.nats.driver.subscriber;

import com.github.fenrir.xmessaging.XMessagingListener;
import com.github.fenrir.xmessaging.MessageProcessCallBack;
import com.github.fenrir.xmessaging.drivers.nats.driver.XNatsDriverBase;
import com.github.fenrir.xmessaging.drivers.nats.driver.XNatsSettings;
import com.github.fenrir.xmessaging.XMessage;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class XNatsListener extends XNatsDriverBase implements XMessagingListener {

    /**
     * receiving queue
     */
    @Getter @Setter private BlockingQueue<XMessage> receiveQueue = null;

    /**
     * message process callback
     */
    @Getter @Setter private MessageProcessCallBack messageProcessCallBack = null;

    private XNatsListener(List<String> natsServerAddresses,
                          String topic,
                          XNatsSettings.ConsumeMode consumeMode,
                          MessageProcessCallBack messageProcessCallBack){
        super(natsServerAddresses, topic, consumeMode, null);

        if(consumeMode == XNatsSettings.ConsumeMode.SYNC)
            this.setReceiveQueue(new LinkedBlockingQueue<>());
        else if(consumeMode == XNatsSettings.ConsumeMode.ASYNC)
            this.setMessageProcessCallBack(messageProcessCallBack);
    }

    public XMessage receiveMessage(Integer durationMs){
        if(this.getReceiveQueue() != null) {
            try {
                return this.getReceiveQueue().poll(durationMs, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
                return null;
            }
        }else{
            // TODO throw exception
            return null;
        }
    }

    public void run(){
        this.runReceiver(this.getReceiveQueue(),
                this.getMessageProcessCallBack());
    }

    public void block(){
        try {
            this.getReceivingThread().join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    static public XNatsListener create(List<String> natsServerAddresses,
                                       String topic,
                                       XNatsSettings.ConsumeMode consumeMode,
                                       MessageProcessCallBack messageProcessCallBack){
        return new XNatsListener(natsServerAddresses, topic, consumeMode, messageProcessCallBack);
    }
}
