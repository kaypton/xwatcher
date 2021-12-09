package com.github.fenrir.xcommon.clients.xlocalmonitor.types;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public enum LocalMonitorType {
    PM("PhysicalMachine"),
    Kubernetes("Kubernetes");

    private final String value;
    static private final Map<String, LocalMonitorType> MAP = new ConcurrentHashMap<>();

    static {
        for(LocalMonitorType type : values()){
            MAP.put(type.toString(), type);
        }
    }

    LocalMonitorType(final String value){
        this.value = value;
    }

    public String getStringValue(){
        return value;
    }

    @Override
    public String toString(){
        return this.getStringValue();
    }

    static public LocalMonitorType from(String name){
        return MAP.get(name);
    }
}
