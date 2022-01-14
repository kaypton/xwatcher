package com.github.fenrir.xtraceprocessor.services;

import com.alibaba.fastjson.JSON;
import com.github.fenrir.xmessaging.MessageProcessCallBack;
import com.github.fenrir.xmessaging.XMessage;
import com.github.fenrir.xmessaging.XMessaging;
import com.github.fenrir.xmessaging.XMessagingListener;
import com.github.fenrir.xtraceprocessor.XTraceProcessorApplication;
import com.github.fenrir.xtraceprocessor.entities.trace.OpenTelemetryTraceData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Service
public class CollectorService {
    private static final Logger LOGGER = LoggerFactory.getLogger(CollectorService.class);

    static public String natsTopic = null;

    private final ProcessorService processorService;

    private final ThreadPoolExecutor listenerExecutor =
            new ThreadPoolExecutor(1, 1, 0, TimeUnit.SECONDS, new LinkedBlockingDeque<>());

    private static class TraceDataProcessor implements MessageProcessCallBack {

        private final ProcessorService processorService;

        public TraceDataProcessor(ProcessorService processorService){
            this.processorService = processorService;
        }

        private final ThreadPoolExecutor executor = new ThreadPoolExecutor(
                20, 10000, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<>()
        );

        @Override
        public void processMessage(XMessage msg) {
            this.executor.submit(() -> {
                String traceStr = msg.getStringData();
                if(traceStr != null){
                    OpenTelemetryTraceData traceData = JSON.parseObject(traceStr, OpenTelemetryTraceData.class);
                    processorService.reportOtelTraceData(traceData);
                }
            });
        }
    }

    public CollectorService(@Autowired ProcessorService processorService){
        this.processorService = processorService;
    }

    public void startup(){
        LOGGER.info("receiver startup ...");
        XMessaging xMessaging = XTraceProcessorApplication.context.getBean("xmessaging", XMessaging.class);
        this.listenerExecutor.submit(()->{
            XMessagingListener listener = xMessaging.getListener(natsTopic, new TraceDataProcessor(this.processorService));
            listener.block();
        });
    }
}
