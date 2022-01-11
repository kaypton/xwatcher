package com.github.fenrir.xtraceprocessor.processors.traceBuilder;

import com.github.fenrir.xtraceprocessor.protobuf.Trace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TraceGraph {
    private static final Logger LOGGER = LoggerFactory.getLogger(TraceGraph.class);

    private final String traceId;

    private TraceNode root = null;
    private final Map<String, TraceNode> orphans = new ConcurrentHashMap<>();

    private final Instant createTime;

    private Map<String, TraceNode> traceNodeMap = new ConcurrentHashMap<>();

    public TraceGraph(String traceId){
        this.traceId = traceId;

        this.createTime = Instant.now();
    }

    public String getTraceId(){
        return this.traceId;
    }

    public void addTraceNode(TraceNode node){
        if(node.getParentSpanId() == null || node.getParentSpanId().equals("")){
            if(this.root != null){
                LOGGER.warn("TraceGraph {} has multiple root", this.traceId);
            }
            this.root = node;
        }else{
            if(this.traceNodeMap.containsKey(node.getParentSpanId())){
                //LOGGER.info("addTraceNode before: {} {}", this.traceNodeMap.get(node.getParentSpanId()).getNodes().size(), node.getParentSpanId());
                this.traceNodeMap.get(node.getParentSpanId()).insertChildSpan(node);
                //LOGGER.info("addTraceNode after: {} {}", this.traceNodeMap.get(node.getParentSpanId()).getNodes().size(), node.getParentSpanId());
            }else{
                this.orphans.put(node.getSpanId(), node);
            }
        }

        this.traceNodeMap.put(node.getSpanId(), node);
    }

    public void claimOrphan(){

        for(String spanId : this.orphans.keySet()){
            if(this.traceNodeMap.containsKey(this.orphans.get(spanId).getParentSpanId())){
                synchronized (this.orphans) {
                    TraceNode orphan = this.orphans.getOrDefault(spanId, null);
                    if(orphan != null){
                        this.orphans.remove(spanId);
                        //LOGGER.info("claim before: {} {}", this.traceNodeMap.get(parentId).getNodes().size(), parentId);
                        this.traceNodeMap.get(orphan.getParentSpanId()).insertChildSpan(orphan);
                        //LOGGER.info("claim after: {} {}", this.traceNodeMap.get(parentId).getNodes().size(), parentId);
                    }
                }
            }
        }
    }

    public boolean checkIntegrityOrTimeout(Duration timeout){
        this.claimOrphan();

        long timeoutSeconds = timeout.getSeconds();
        long duration = Instant.now().getEpochSecond() - this.createTime.getEpochSecond();

        if(timeoutSeconds <= duration){
            LOGGER.info("check timeout, orphans {}", this.orphans.size());
            return true;
        }

        if(this.root == null){
            return false;
        }

        for(String key : this.traceNodeMap.keySet()){
            if(!this.traceNodeMap.get(key).checkIntegrity()){
                LOGGER.info("span service: {} interfaceURI: {} not integrity",
                        this.traceNodeMap.get(key).getSpanServiceName(),
                        this.traceNodeMap.get(key).getSpanInterfaceURI());
                return false;
            }
        }

        LOGGER.info("integrity");
        return true;
    }

    public Trace.TraceGraph getProtobuf(){
        System.out.println(this.traceNodeMap.size());
        Trace.TraceGraph.Builder traceGraphBuilder = Trace.TraceGraph.newBuilder();
        for(String key : this.orphans.keySet()){
            traceGraphBuilder.addOrphans(this.orphans.get(key).getProtobuf());
        }
        return traceGraphBuilder.setRoot(this.root.getProtobuf()).build();
    }
}
