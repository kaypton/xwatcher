package com.github.fenrir.xdataflowtrace;

import com.github.fenrir.xmessaging.*;
import org.apache.flink.streaming.api.functions.source.ParallelSourceFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class NatsParallelSourceFunction implements ParallelSourceFunction<String> {
    private final static Logger LOGGER = LoggerFactory.getLogger(NatsParallelSourceFunction.class);

    private final BlockingQueue<String> queue = new LinkedBlockingQueue<>();
    private final XMessaging xMessaging;
    private final String natsSubject;

    private static class NatsMessageProcessCallback implements MessageProcessCallBack {

        private final BlockingQueue<String> queue;

        public NatsMessageProcessCallback(BlockingQueue<String> queue){
            this.queue = queue;
        }

        @Override
        public void processMessage(XMessage msg) {
            try {
                queue.put(msg.getStringData());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public NatsParallelSourceFunction(String natsAddresses, String subject){
        this.xMessaging = XMessaging.create(new XMessagingConfiguration(natsAddresses));
        this.natsSubject = subject;
        LOGGER.info("Connect to {}", natsAddresses);
    }
    @Override
    public void run(SourceContext<String> sourceContext) throws Exception {
        this.xMessaging.getListener(
                this.natsSubject, new NatsMessageProcessCallback(this.queue));
        while(true){
            String str = this.queue.take();
            sourceContext.collect(str);
        }
    }

    @Override
    public void cancel() {

    }
}
