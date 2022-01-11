package com.github.fenrir.xtraceprocessor.processors.traceBuilder;

import com.github.fenrir.xtraceprocessor.entities.serviceDependency.Span;
import com.github.fenrir.xtraceprocessor.protobuf.Trace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;

public class TraceNode {
    private static final Logger LOGGER = LoggerFactory.getLogger(TraceNode.class);

    private final LinkedList<TraceNode> startTimeSortedChildNodeList = new LinkedList<>();
    private final LinkedList<TraceNode> endTimeSortedChildNodeList = new LinkedList<>();
    private final Object nodeListLock = new Object();

    private TraceNode parent = null;

    private final Span span;

    public TraceNode(Span span){
        this.span = span;
    }

    public int getSubCallNum(){
        return this.span.getSubCallNum();
    }

    public void insertChildSpan(TraceNode node){
        if(node.getParentSpanId() == null || node.getParentSpanId().equals("")){
            LOGGER.info("this is a root span, but not a child");
            return;
        }
        if(!node.getParentSpanId().equals(this.span.getSpanId())){
            LOGGER.warn("span {} parent span is {}, but not {}", node.getSpanId(), node.getParentSpanId(), this.span.getSpanId());
            return;
        }

        synchronized (nodeListLock) {
            boolean over1 = false;
            boolean over2 = false;
            for(int i = 0; i < this.startTimeSortedChildNodeList.size(); i++){
                if(this.startTimeSortedChildNodeList.get(i).getStartTimeNano() < node.getStartTimeNano() && !over1){
                    this.startTimeSortedChildNodeList.add(i, node);
                    over1 = true;
                }
                if(this.endTimeSortedChildNodeList.get(i).getEndTimeNano() < node.getEndTimeNano() && !over2){
                    this.endTimeSortedChildNodeList.add(i, node);
                    over2 = true;
                }
                if(over1 && over2)
                    break;
            }
            if(!over1)
                this.startTimeSortedChildNodeList.addLast(node);
            if(!over2)
                this.endTimeSortedChildNodeList.addLast(node);
        }
    }

    public int getChildSpanNum(){
        return this.startTimeSortedChildNodeList.size();
    }

    public String getSubCallUUID(){
        return this.span.getSubCallUUID();
    }

    public String getSpanServiceName(){
        return this.span.getServiceName();
    }

    public String getSpanInterfaceURI(){
        return this.span.getInterfaceURI();
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
        return this.span.getParentSpanId();
    }

    public boolean checkIntegrity(){
        //LOGGER.info("{} vs. {}", this.span.getSubCallNum(), this.startTimeSortedChildNodeList.size());
        return this.span.getSubCallNum() == this.startTimeSortedChildNodeList.size();
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

    public Trace.TraceNode getProtobuf(){
        Trace.TraceNode.Builder builder = Trace.TraceNode.newBuilder();
        builder.setSpan(this.span.getProtobuf());
        for(TraceNode child : this.startTimeSortedChildNodeList){
            builder.addChildren(child.getProtobuf());
        }
        return builder.build();
    }
}
