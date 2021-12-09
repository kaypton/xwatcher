package com.github.fenrir.xlocalmonitor.services.prometheus;

import com.github.fenrir.xcommon.clients.xlocalmonitor.types.LocalMonitorType;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public enum MetricType {
    COUNTER("counter"),
    GAUGE("gauge"),
    SUMMARY("summary"),
    HISTOGRAM("histogram");

    private final String value;
    static private final Map<String, MetricType> MAP = new ConcurrentHashMap<>();

    static {
        for(MetricType type : values()){
            MAP.put(type.toString(), type);
        }
    }

    MetricType(final String value){
        this.value = value;
    }

    public String getStringValue(){
        return value;
    }

    @Override
    public String toString(){
        return this.getStringValue();
    }

    static public MetricType from(String name){
        return MAP.get(name);
    }
}
