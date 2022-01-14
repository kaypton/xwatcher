package com.github.fenrir.xdataflowtrace.entity.trace;

import com.github.fenrir.xcommon.utils.Tuple2;
import com.github.fenrir.xdataflowtrace.protobuf.Trace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

public class Span {
    static private final Logger LOGGER = LoggerFactory.getLogger(Span.class);

    private String serviceName;
    private String interfaceName;
    private String interfaceURI;

    private String traceId;
    private String spanId;
    private String kind;
    private String parentSpanId;
    private Double startTimeNano;
    private Double endTimeNano;
    private String subCallUUID;
    private String instanceUUID;

    private final LinkedList<SubCall> subCalls = new LinkedList<>();

    private Span(){}

    public int getSubCallNum(){
        return this.subCalls.size();
    }

    public Iterator<SubCall> getEventsIterator(){
        return this.subCalls.iterator();
    }

    public void addEvent(SubCall subCall){
        this.subCalls.add(subCall);
    }

    public Iterator<SubCall> getSubCallIterator(){
        return this.subCalls.iterator();
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getSpanId() {
        return spanId;
    }

    public void setSpanId(String spanId) {
        this.spanId = spanId;
    }

    public Double getStartTimeNano() {
        return startTimeNano;
    }

    public void setStartTimeNano(Double startTimeNano) {
        this.startTimeNano = startTimeNano;
    }

    public Double getEndTimeNano() {
        return endTimeNano;
    }

    public void setEndTimeNano(Double endTimeNano) {
        this.endTimeNano = endTimeNano;
    }

    public String getInterfaceURI() {
        return interfaceURI;
    }

    public void setInterfaceURI(String interfaceURI) {
        this.interfaceURI = interfaceURI;
    }

    public String getParentSpanId() {
        return parentSpanId;
    }

    public void setParentSpanId(String parentSpanId) {
        this.parentSpanId = parentSpanId;
    }

    public String getSubCallUUID() {
        return subCallUUID;
    }

    public void setSubCallUUID(String subCallUUID) {
        this.subCallUUID = subCallUUID;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getInstanceUUID() {
        return instanceUUID;
    }

    public void setInstanceUUID(String instanceUUID) {
        this.instanceUUID = instanceUUID;
    }

    static public Span create(OpenTelemetryTraceData.Resource resource, OpenTelemetryTraceData.Span data){
        Span span = new Span();

        String serviceName = data.name;
        String interfaceURI = null;
        String subCallUUID = null;
        for(OpenTelemetryTraceData.Attribute attribute : data.attributes){
            if(attribute.key.equals("uri")){
                interfaceURI = attribute.value.stringValue;
            }
            if(attribute.key.equals("subCallUUID")){
                subCallUUID = attribute.value.stringValue;
            }
        }
        if(interfaceURI == null){
            LOGGER.error("span interfaceURI is null");
            return null;
        }

        span.setServiceName(serviceName);

        span.setInterfaceName(interfaceURI);
        span.setInterfaceURI(interfaceURI);
        span.setSubCallUUID(subCallUUID);

        span.setKind(data.kind);
        span.setSpanId(data.spanId);
        span.setParentSpanId(data.parentSpanId);
        span.setTraceId(data.traceId);
        span.setStartTimeNano(data.startTimeUnixNano);
        span.setEndTimeNano(data.endTimeUnixNano);

        for(OpenTelemetryTraceData.Attribute attribute : resource.attributes){
            if(attribute.key.equals("uuid")){
                span.setInstanceUUID(attribute.value.stringValue);
            }
        }

        if(data.events == null || data.events.length == 0){
            return span;
        }

        Map<String, Tuple2<Double, Double>> eventMap = new HashMap<>();

        /* parse sub calls */
        for(OpenTelemetryTraceData.Event otelEvent : data.events){
            if(!otelEvent.name.equals("SubCall")){
                continue;
            }
            String downstreamServiceName = null;
            String action = null;
            String uuid = null;
            String downstreamInterfaceURI = null;

            for(OpenTelemetryTraceData.Attribute attribute : otelEvent.attributes){
                if(attribute.key.equals("action")){
                    action = attribute.value.stringValue;
                }else if(attribute.key.equals("service.name")){
                    downstreamServiceName = attribute.value.stringValue;
                }else if(attribute.key.equals("uri")){
                    downstreamInterfaceURI = attribute.value.stringValue;
                }else if(attribute.key.equals("uuid")){
                    uuid = attribute.value.stringValue;
                }
            }

            if(downstreamInterfaceURI == null || action == null || uuid == null || downstreamServiceName == null){
                LOGGER.error("some subcall info is null");
                return null;
            }

            String label = downstreamServiceName + ":" + downstreamInterfaceURI + ":" + uuid;

            if(eventMap.containsKey(label)){
                if(action.equals("start")){
                    if(eventMap.get(label).second != -1){
                        SubCall subCall = new SubCall();
                        subCall.setServiceName(downstreamServiceName);
                        subCall.setInterfaceName(downstreamInterfaceURI);
                        subCall.setInterfaceURI(downstreamInterfaceURI);
                        subCall.setStartTimeNano(otelEvent.timeUnixNano);
                        subCall.setEndTimeNano(eventMap.get(label).second);
                        subCall.setUuid(uuid);
                        span.addEvent(subCall);
                    }
                }else{
                    if(eventMap.get(label).first != -1){
                        SubCall subCall = new SubCall();
                        subCall.setServiceName(downstreamServiceName);
                        subCall.setInterfaceName(downstreamInterfaceURI);
                        subCall.setInterfaceURI(downstreamInterfaceURI);
                        subCall.setStartTimeNano(eventMap.get(label).first);
                        subCall.setEndTimeNano(otelEvent.timeUnixNano);
                        subCall.setUuid(uuid);
                        span.addEvent(subCall);
                    }
                }
            }else{
                eventMap.put(label, action.equals("start") ?
                        new Tuple2<>(otelEvent.timeUnixNano, (double) -1) :
                        new Tuple2<>((double) -1, otelEvent.timeUnixNano));
            }
        }

        return span;
    }

    public Trace.Span getProtobuf(){
        Trace.Span.Builder builder = Trace.Span.newBuilder();
        builder.setInterfaceUri(this.interfaceURI)
                .setInterfaceName(this.interfaceName)
                .setInstanceUuid(this.instanceUUID)
                .setSpanId(this.spanId)
                .setTraceId(this.traceId)
                .setParentSpanId(this.parentSpanId)
                .setServiceName(this.serviceName)
                .setStartTimeUnixNano(this.startTimeNano)
                .setEndTimeUnixNano(this.endTimeNano)
                .setKind(this.kind);
        if(this.subCallUUID != null)
            builder.setSubcallUuid(this.subCallUUID);
        for(SubCall subCall : this.subCalls){
            builder.addSubcalls(subCall.getProtobuf());
        }
        return builder.build();
    }
}
