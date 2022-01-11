package com.github.fenrir.xtraceprocessor.processors.traceBuilder;

import com.github.fenrir.xmessaging.XMessageBuilder;
import com.github.fenrir.xmessaging.XMessaging;
import com.github.fenrir.xmessaging.XMessagingPublisher;
import com.github.fenrir.xtraceprocessor.XTraceProcessorApplication;
import com.github.fenrir.xtraceprocessor.entities.serviceDependency.Span;
import com.github.fenrir.xtraceprocessor.processors.Processor;
import com.github.fenrir.xtraceprocessor.protobuf.Trace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.*;

public class TraceBuilderProcessor extends Processor {
    static private final Logger LOGGER = LoggerFactory.getLogger(TraceBuilderProcessor.class);

    private final Map<String, TraceGraph> traceGraphMap = new ConcurrentHashMap<>();

    private XMessagingPublisher xMessagingPublisher = null;

    private final ThreadPoolExecutor exportExecutor = new ThreadPoolExecutor(
            10, 10000, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<>()
    );

    private final Timer exportTimer = new Timer();

    public TraceBuilderProcessor(){
        exportTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                exportTraceGraphAll();
            }
        }, 0, 1000);
    }

    @Override
    protected Span internalDoProcess(Span span) {
        if(this.xMessagingPublisher == null){
            XMessaging xMessaging = XTraceProcessorApplication.context.getBean("xmessaging", XMessaging.class);
            this.xMessagingPublisher = xMessaging.getPublisher("trace.graph");
        }

        synchronized (this.traceGraphMap) {
            this.buildTraceGraph(span);
        }

        return span;
    }

    private void buildTraceGraph(Span span){
        String traceId = span.getTraceId();
        if(this.traceGraphMap.containsKey(traceId)){
            this.traceGraphMap.get(traceId).addTraceNode(new TraceNode(span));
        }else{
            TraceGraph graph = new TraceGraph(traceId);
            graph.addTraceNode(new TraceNode(span));
            this.traceGraphMap.put(traceId, graph);
        }
    }

    private void exportTraceGraphAll(){
        for(String traceId : this.traceGraphMap.keySet()){
            this.exportExecutor.submit(() -> {
                this.exportTraceGraph(traceId);
            });
        }
    }

    private void exportTraceGraph(String traceId){
        TraceGraph traceGraph = this.traceGraphMap.remove(traceId);
        if(traceGraph != null){
            if(traceGraph.checkIntegrityOrTimeout(Duration.ofSeconds(20))){
                ByteArrayOutputStream bos = new ByteArrayOutputStream();

                Trace.TraceGraph graph = traceGraph.getProtobuf();
                try {
                    graph.writeTo(bos);
                    this.xMessagingPublisher.send(XMessageBuilder.builder("trace.graph")
                            .setPayload(bos.toByteArray())
                            .buildNatsMessage());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else{
                this.traceGraphMap.put(traceId, traceGraph);
            }
        }
    }
}
