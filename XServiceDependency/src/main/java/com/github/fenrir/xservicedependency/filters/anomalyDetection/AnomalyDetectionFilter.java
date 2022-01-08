package com.github.fenrir.xservicedependency.filters.anomalyDetection;

import com.github.fenrir.xservicedependency.entities.serviceDependency.Span;
import com.github.fenrir.xservicedependency.filters.Filter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AnomalyDetectionFilter extends Filter {

    private Map<String, TraceGraph> traceGraphMap = new ConcurrentHashMap<>();

    @Override
    protected Span internalDoFilter(Span span) {
        String traceId = span.getTraceId();
        if(this.traceGraphMap.containsKey(traceId)){
            this.traceGraphMap.get(traceId).addTraceNode(new TraceNode(span));
        }else{
            TraceGraph graph = new TraceGraph(traceId);
            graph.addTraceNode(new TraceNode(span));
            this.traceGraphMap.put(traceId, graph);
        }
        return span;
    }
}
