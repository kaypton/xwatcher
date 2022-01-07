package com.github.fenrir.xservicedependency.entities.serviceDependency;

public class Event {
    private String serviceName;
    private String interfaceName;
    private Double startTimeNano;
    private Double endTimeNano;

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
}
