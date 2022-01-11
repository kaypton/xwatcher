package com.github.fenrir.xservicedependency.services;

import com.alibaba.fastjson.JSON;
import com.github.fenrir.xmessaging.MessageProcessCallBack;
import com.github.fenrir.xmessaging.XMessage;
import com.github.fenrir.xmessaging.XMessaging;
import com.github.fenrir.xmessaging.XMessagingListener;
import com.github.fenrir.xservicedependency.XServiceDependencyApplication;
import com.github.fenrir.xservicedependency.entities.serviceDependency.Span;
import com.github.fenrir.xservicedependency.entities.trace.OpenTelemetryTraceData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Service
public class ReceiveService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReceiveService.class);

    static public String natsTopic = null;

    private final DependencyService dependencyService;

    private final ThreadPoolExecutor listenerExecutor =
            new ThreadPoolExecutor(1, 1, 0, TimeUnit.SECONDS, new LinkedBlockingDeque<>());

    private static class TraceDataProcessor implements MessageProcessCallBack {

        private final DependencyService dependencyService;

        public TraceDataProcessor(DependencyService dependencyService){
            this.dependencyService = dependencyService;
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
                    dependencyService.reportOtelTraceData(traceData);
                }
            });
        }
    }

    public ReceiveService(@Autowired DependencyService dependencyService){
        this.dependencyService = dependencyService;
    }

    public void startup(){
        LOGGER.info("receiver startup ...");
        XMessaging xMessaging = XServiceDependencyApplication.context.getBean("xmessaging", XMessaging.class);
        this.listenerExecutor.submit(()->{
            XMessagingListener listener = xMessaging.getListener(natsTopic, new TraceDataProcessor(this.dependencyService));
            listener.block();
        });
    }
}
