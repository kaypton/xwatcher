package com.github.fenrir.xtraceprocessor.processors.persistence;

import com.github.fenrir.xtraceprocessor.configs.URISelectorConfig;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Service {
    private String name;
    private final Map<String, Interface> interfaceMap = new ConcurrentHashMap<>();

    private final URISelectorConfig selectorConfig;

    public Service(URISelectorConfig selectorConfig){
        this.selectorConfig = selectorConfig;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, Interface> getInterfaceMap() {
        return interfaceMap;
    }

    public Interface getInterface(String interfaceName){
        return this.getInterfaceMap().getOrDefault(interfaceName, null);
    }

    public void updateInterface(String interfaceName, String interfaceURI, double startTime, double endTime, double serviceTime){
        interfaceName = this.selectorConfig.match(this.getName(), interfaceName);
        interfaceURI = interfaceName;
        synchronized (this.interfaceMap) {
            if(!this.interfaceMap.containsKey(interfaceName)){
                Interface i = new Interface();
                i.setName(interfaceName);
                i.setService(this);
                i.setUri(interfaceURI);
                this.interfaceMap.put(interfaceName, i);
            }
        }
        this.interfaceMap.get(interfaceName).addServiceTimeNano(startTime, endTime, serviceTime);
    }

    public void updateDownstreamInterface(String interfaceName,
                                          String interfaceURI,
                                          Service downstreamService,
                                          String downstreamInterfaceName,
                                          String downstreamInterfaceURI,
                                          double startTime,
                                          double endTime,
                                          double responseTime){
        if(downstreamService == null) return;
        interfaceName = this.selectorConfig.match(this.getName(), interfaceName);
        interfaceURI = interfaceName;

        downstreamInterfaceName = this.selectorConfig.match(downstreamService.getName(), downstreamInterfaceName);
        downstreamInterfaceURI = downstreamInterfaceName;

        synchronized (this.interfaceMap) {
            if(!this.interfaceMap.containsKey(interfaceName)){
                Interface i = new Interface();
                i.setName(interfaceName);
                i.setService(this);
                i.setUri(interfaceURI);
                this.interfaceMap.put(i.getName(), i);
            }
        }
        Interface i = downstreamService.getInterface(downstreamInterfaceName);
        if(i != null)
            this.interfaceMap.get(interfaceName).addDownstreamResponseTime(i, startTime, endTime, responseTime);

    }
}
