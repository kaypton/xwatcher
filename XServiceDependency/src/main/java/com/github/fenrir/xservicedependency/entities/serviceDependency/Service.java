package com.github.fenrir.xservicedependency.entities.serviceDependency;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Service {
    private String name;
    private final Map<String, Interface> interfaceMap = new ConcurrentHashMap<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, Interface> getInterfaceMap() {
        return interfaceMap;
    }

    public void updateInterface(String interfaceName, Long startTime, Long endTime, Long serviceTime){
        if(!this.interfaceMap.containsKey(interfaceName)){
            Interface i = new Interface();
            i.setName(interfaceName);
            i.setService(this);
            this.interfaceMap.put(interfaceName, i);
        }

        this.interfaceMap.get(interfaceName).addServiceTimeNano(startTime, endTime, serviceTime);
    }

    public Interface getInterface(String interfaceName){
        return this.getInterfaceMap().getOrDefault(interfaceName, null);
    }

    public void updateInterfaceDownstreamInterface(String interfaceName,
                                                   Service downstreamService,
                                                   String downstreamInterfaceName,
                                                   Long startTime,
                                                   Long endTime,
                                                   Long responseTime){
        if(downstreamService == null) return;
        if(!this.interfaceMap.containsKey(interfaceName)){
            Interface i = new Interface();
            i.setName(interfaceName);
            i.setService(this);
            this.interfaceMap.put(i.getName(), i);
        }

        Interface i = downstreamService.getInterface(downstreamInterfaceName);
        if(i != null)
            this.interfaceMap.get(interfaceName).addDownstreamResponseTime(i, startTime, endTime, responseTime);

    }
}
