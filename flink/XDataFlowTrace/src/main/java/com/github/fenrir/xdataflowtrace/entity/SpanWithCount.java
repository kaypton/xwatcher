package com.github.fenrir.xdataflowtrace.entity;

import com.github.fenrir.xdataflowtrace.entity.trace.Span;

public class SpanWithCount {
    public long count;
    public Span span;

    public SpanWithCount(){}

    public SpanWithCount(Span span){
        this.span = span;
        this.count = 1;
    }

    public String toString(){
        return "SpanWithCount" + "{" +
                "interfaceName=" + this.span.getInterfaceName() + "," +
                "interfaceURI=" + this.span.getInterfaceURI() + "," +
                "serviceName=" + this.span.getServiceName() + "," +
                "count=" + this.count +
                "}";
    }
}
