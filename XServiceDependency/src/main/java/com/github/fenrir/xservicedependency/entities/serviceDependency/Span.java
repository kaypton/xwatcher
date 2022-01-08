package com.github.fenrir.xservicedependency.entities.serviceDependency;

import com.github.fenrir.xcommon.utils.Tuple2;
import com.github.fenrir.xservicedependency.entities.trace.OpenTelemetryTraceData;

import java.util.*;

public class Span {

    private String serviceName;
    private String interfaceName;

    private String traceId;
    private String spanId;
    private String parentSpanId;
    private Double startTimeNano;
    private Double endTimeNano;

    private final List<Event> events = new ArrayList<>();

    private Span(){}

    public Iterator<Event> getEventsIterator(){
        return this.events.iterator();
    }

    public void addEvent(Event event){
        this.events.add(event);
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

    public String getParentSpanId() {
        return parentSpanId;
    }

    public void setParentSpanId(String parentSpanId) {
        this.parentSpanId = parentSpanId;
    }

    static public Span create(OpenTelemetryTraceData.Span data){
        Span span = new Span();
        String[] splitSpanName = data.name.split(":");
        String serviceName = splitSpanName[0];
        String interfaceName = splitSpanName[1];

        span.setServiceName(serviceName);
        span.setInterfaceName(interfaceName);

        span.setSpanId(data.spanId);
        span.setParentSpanId(data.parentSpanId);
        span.setTraceId(data.traceId);
        span.setStartTimeNano(data.startTimeUnixNano);
        span.setEndTimeNano(data.endTimeUnixNano);

        Map<String, Tuple2<Double, Double>> eventMap = new HashMap<>();

        /* parse events */
        for(OpenTelemetryTraceData.Event otelEvent : data.events){
            String[] splitEventName = otelEvent.name.split(":");
            String downstreamServiceName = splitEventName[0];
            String action = splitEventName[1];
            String uuid = splitEventName[2];
            String downstreamInterfaceName = splitEventName[3];

            String label = downstreamServiceName + ":" + downstreamInterfaceName + ":" + uuid;

            if(eventMap.containsKey(label)){
                if(action.equals("start")){
                    if(eventMap.get(label).second != -1){
                        Event event = new Event();
                        event.setServiceName(downstreamServiceName);
                        event.setInterfaceName(downstreamInterfaceName);
                        event.setStartTimeNano(otelEvent.timeUnixNano);
                        event.setEndTimeNano(eventMap.get(label).second);
                        span.addEvent(event);
                    }
                }else{
                    if(eventMap.get(label).first != -1){
                        Event event = new Event();
                        event.setServiceName(downstreamServiceName);
                        event.setInterfaceName(downstreamInterfaceName);
                        event.setStartTimeNano(eventMap.get(label).first);
                        event.setEndTimeNano(otelEvent.timeUnixNano);
                        span.addEvent(event);
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
}
