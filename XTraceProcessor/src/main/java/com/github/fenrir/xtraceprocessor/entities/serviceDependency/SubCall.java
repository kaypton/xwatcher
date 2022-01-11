package com.github.fenrir.xservicedependency.entities.serviceDependency;

import com.github.fenrir.xservicedependency.protobuf.Trace;

public class SubCall {
    private String serviceName;
    private String interfaceName;
    private String interfaceURI;
    private Double startTimeNano;
    private Double endTimeNano;
    private String uuid;

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String service) {
        this.serviceName = service;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String inter) {
        this.interfaceName = inter;
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

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getInterfaceURI() {
        return interfaceURI;
    }

    public void setInterfaceURI(String interfaceURI) {
        this.interfaceURI = interfaceURI;
    }

    public Trace.SubCall getProtobuf(){
        return Trace.SubCall.newBuilder()
                .setServiceName(this.serviceName)
                .setInterfaceName(this.interfaceName)
                .setInterfaceUri(this.interfaceURI)
                .setStartTimeUnixNano(this.startTimeNano)
                .setEndTimeUnixNano(this.endTimeNano)
                .setSubcallUuid(this.uuid)
                .build();
    }
}
