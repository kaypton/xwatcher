package com.github.fenrir.xservicedependency.filters.anomalyDetection;

import com.github.fenrir.xservicedependency.entities.serviceDependency.Span;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;

public class TraceNode {
    private static final Logger LOGGER = LoggerFactory.getLogger(TraceNode.class);

    private final LinkedList<TraceNode> startTimeSortedChildNodeList = new LinkedList<>();
    private final LinkedList<TraceNode> endTimeSortedChildNodeList = new LinkedList<>();

    private TraceNode parent = null;

    private final Span span;

    public TraceNode(Span span){
        this.span = span;
    }

    public void insertChildSpan(TraceNode node){
        if(node.getParentSpanId() == null){
            return;
        }
        if(!node.getParentSpanId().equals(this.span.getSpanId())){
            LOGGER.warn("span {} parent span is {}, but not {}", node.getSpanId(), node.getParentSpanId(), this.span.getSpanId());
            return;
        }

        for(int i = 0; i < this.startTimeSortedChildNodeList.size(); i++){
            if(this.startTimeSortedChildNodeList.get(i).getStartTimeNano() > node.getStartTimeNano()){
                if(i == 0)
                    this.startTimeSortedChildNodeList.addFirst(node);
                else this.startTimeSortedChildNodeList.add(i - 1, node);
            }
            if(this.endTimeSortedChildNodeList.get(i).getEndTimeNano() > node.getEndTimeNano()){
                if(i == 0)
                    this.endTimeSortedChildNodeList.addFirst(node);
                else this.endTimeSortedChildNodeList.add(i - 1, node);
            }
        }
    }

    public int getChildSpanNum(){
        return this.startTimeSortedChildNodeList.size();
    }

    public TraceNode getFirstNodeSortedByStartTime(){
        return this.startTimeSortedChildNodeList.getFirst();
    }

    public TraceNode getLastNodeSortedByStartTime(){
        return this.startTimeSortedChildNodeList.getLast();
    }

    public TraceNode getFirstNodeSortedByEndTime(){
        return this.endTimeSortedChildNodeList.getFirst();
    }

    public TraceNode getLastNodeSortedByEndTime(){
        return this.endTimeSortedChildNodeList.getLast();
    }

    public LinkedList<TraceNode> getNodes(){
        return this.startTimeSortedChildNodeList;
    }

    public String getParentSpanId(){
        if(this.parent != null)
            return this.parent.getSpanId();
        return null;
    }

    public void setParent(TraceNode span){
        this.parent = span;
    }

    public TraceNode getParent(){
        return this.parent;
    }

    public String getSpanId(){
        return this.span.getSpanId();
    }

    public Double getStartTimeNano(){
        return this.span.getStartTimeNano();
    }

    public Double getEndTimeNano(){
        return this.span.getEndTimeNano();
    }
}
