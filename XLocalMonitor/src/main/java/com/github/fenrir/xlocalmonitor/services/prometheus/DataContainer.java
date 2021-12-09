package com.github.fenrir.xlocalmonitor.services.prometheus;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DataContainer {
    protected String name;

    private Map<String, Data> dataMap = new ConcurrentHashMap<>();

    public DataContainer(String name){
        this.name = name;
    }

    public String getName(){
        return this.name;
    }

    public void unregisterData(String id){
        this.dataMap.remove(id);
    }

    public void registerData(Data data){
        if(data == null) return;
        this.dataMap.put(data.getId(), data);
    }

    public String getMetricPlainTextString(){
        StringBuilder sb = new StringBuilder();
        for(String dataId : this.dataMap.keySet()){
            String str = this.dataMap.get(dataId).getMetricPlainTextString();
            if(str != null)
                sb.append(str).append("\n");
        }
        return sb.toString();
    }
}
