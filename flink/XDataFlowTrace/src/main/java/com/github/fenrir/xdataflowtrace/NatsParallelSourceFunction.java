package com.github.fenrir.xdataflowtrace;

import com.alibaba.fastjson.JSON;
import com.github.fenrir.xdataflowtrace.entity.trace.OpenTelemetryTraceData;
import com.github.fenrir.xdataflowtrace.entity.trace.Span;
import com.github.fenrir.xmessaging.*;
import org.apache.flink.streaming.api.functions.source.ParallelSourceFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class NatsParallelSourceFunction implements ParallelSourceFunction<Span> {
    private final static Logger LOGGER = LoggerFactory.getLogger(NatsParallelSourceFunction.class);

    private final BlockingQueue<Span> queue = new LinkedBlockingQueue<>();
    private final XMessaging xMessaging;
    private final String natsSubject;

    private boolean cancel = false;

    private static class NatsMessageProcessCallback implements MessageProcessCallBack {

        private final BlockingQueue<Span> queue;

        public NatsMessageProcessCallback(BlockingQueue<Span> queue){
            this.queue = queue;
        }

        private final ThreadPoolExecutor processExecutor = new ThreadPoolExecutor(
                100, 1000, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<>()
        );

        @Override
        public void processMessage(XMessage msg) {
            OpenTelemetryTraceData traceData = JSON.parseObject(msg.getStringData(), OpenTelemetryTraceData.class);
            processExecutor.submit(() -> {
                for(OpenTelemetryTraceData.ResourceSpan resourceSpan : traceData.resourceSpans){
                    for(OpenTelemetryTraceData.InstrumentationLibrarySpan instrumentationLibrarySpan : resourceSpan.instrumentationLibrarySpans){
                        for(int i = 0; i < instrumentationLibrarySpan.spans.length; i++){
                            Span _span = Span.create(resourceSpan.resource, instrumentationLibrarySpan.spans[i]);
                            if(_span != null)
                                try {
                                    synchronized (this.queue) {
                                        this.queue.put(_span);
                                    }
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                        }
                    }
                }
            });
        }
    }

    public NatsParallelSourceFunction(String natsAddresses, String subject){
        this.xMessaging = XMessaging.create(new XMessagingConfiguration(natsAddresses));
        this.natsSubject = subject;
        LOGGER.info("Connect to {}", natsAddresses);
    }
    @Override
    public void run(SourceContext<Span> sourceContext) throws Exception {
        this.xMessaging.getListener(this.natsSubject, new NatsMessageProcessCallback(this.queue));
        while(!cancel){
            sourceContext.collect(this.queue.take());
        }
    }

    @Override
    public void cancel() {
        this.cancel = true;
    }
}
