package com.github.fenrir.xcommon.clients.xregistry.types.rest.constants;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public enum RegisterStatus {
    OK("OK"),
    EXIST("exist");

    private final String value;
    static private final Map<String, RegisterStatus> MAP = new ConcurrentHashMap<>();

    static {
        for(RegisterStatus type : values()){
            MAP.put(type.toString(), type);
        }
    }

    RegisterStatus(final String value){
        this.value = value;
    }

    public String getStringValue(){
        return value;
    }

    @Override
    public String toString(){
        return this.getStringValue();
    }

    static public RegisterStatus from(String name){
        return MAP.get(name);
    }
}
