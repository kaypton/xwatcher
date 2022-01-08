package com.github.fenrir.xservicedependency.filters.anomalyDetection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;

public class TraceGraph {
    private static final Logger LOGGER = LoggerFactory.getLogger(TraceGraph.class);

    private final String traceId;

    private TraceNode root = null;
    private final LinkedList<TraceNode> orphans = new LinkedList<>();

    public TraceGraph(String traceId){
        this.traceId = traceId;
    }

    public String getTraceId(){
        return this.traceId;
    }

    public void addTraceNode(TraceNode node){
        if(node.getParentSpanId().equals("")){
            if(this.root != null){
                LOGGER.warn("TraceGraph {} has multiple root", this.traceId);
            }
            this.root = node;
        }else{
            boolean ok = this.addNonRootTraceNode(node, this.root);
            if(!ok)
                this.orphans.addLast(node);
        }

        this.claimOrphan(node);
    }

    private boolean addNonRootTraceNode(TraceNode node, TraceNode target){
        if(target == null){
            return false;
        }else{
            if(target.getSpanId().equals(node.getParentSpanId())){
                target.insertChildSpan(node);
                return true;
            }else{
                for(TraceNode subTargetNode : target.getNodes()){
                    boolean ok = addNonRootTraceNode(node, subTargetNode);
                    if(ok)
                        return true;
                }
                return false;
            }
        }
    }

    public void claimOrphan(TraceNode node){
        int size = this.orphans.size();
        for(int i = 0; i < size; i++){
            if(this.orphans.get(i).getParentSpanId().equals(node.getSpanId())){
                node.insertChildSpan(this.orphans.get(i));
                this.orphans.remove(i);
                size = this.orphans.size();
            }
        }
    }
}
