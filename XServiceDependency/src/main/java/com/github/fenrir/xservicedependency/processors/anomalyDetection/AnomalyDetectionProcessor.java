package com.github.fenrir.xservicedependency.processors.anomalyDetection;

import com.github.fenrir.xservicedependency.entities.serviceDependency.Span;
import com.github.fenrir.xservicedependency.processors.Processor;
import com.github.fenrir.xservicedependency.processors.anomalyDetection.config.Configuration;
import com.github.fenrir.xservicedependency.processors.anomalyDetection.config.TimeLimit;
import com.github.fenrir.xservicedependency.processors.anomalyDetection.utils.URIUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class AnomalyDetectionProcessor extends Processor {

    private final Map<String, TraceGraph> traceGraphMap = new ConcurrentHashMap<>();

    private final Map<String, Map<String, TimeLimit>> responseTimeLimits;

    private final ThreadPoolExecutor detectionExecutor = new ThreadPoolExecutor(
            10, 100, 5, TimeUnit.SECONDS, new LinkedBlockingDeque<>()
    );

    public AnomalyDetectionProcessor(Configuration config){
        this.responseTimeLimits = config.getResponseTimeLimits();
    }

    @Override
    protected Span internalDoProcess(Span span) {
        synchronized (this.traceGraphMap) {
            String traceId = span.getTraceId();
            if(this.traceGraphMap.containsKey(traceId)){
                this.traceGraphMap.get(traceId).addTraceNode(new TraceNode(span));
            }else{
                TraceGraph graph = new TraceGraph(traceId);
                graph.addTraceNode(new TraceNode(span));
                this.traceGraphMap.put(traceId, graph);
            }
        }

        this.detectionExecutor.submit(() -> {
            this.detect(span);
        });

        return span;
    }

    private void detect(Span span){
        
    }
}
