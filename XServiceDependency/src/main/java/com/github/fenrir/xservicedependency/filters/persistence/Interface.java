package com.github.fenrir.xservicedependency.filters.persistence;

import com.github.fenrir.prometheusclient.services.PrometheusDataPushService;
import com.github.fenrir.xservicedependency.XServiceDependencyApplication;
import com.github.fenrir.xservicedependency.configs.ApplicationConfig;
import com.github.fenrir.xservicedependency.entities.influxDB.InterfaceRTNano;
import com.github.fenrir.xservicedependency.entities.influxDB.InterfaceSTNano;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.domain.WritePrecision;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Interface {
    /**
     * interface name
     */
    private String name;

    /**
     * the service which contains the interface
     */
    private Service service;

    // service name -> tuple(interface object, response time list)
    // tuple(startTime:Long, endTime:Long, responseTime:Long)
    private final Map<String, Map<String, Interface>> downstreamInterfaceMap =
            new ConcurrentHashMap<>();

    private InfluxDBClient influxDBClient;

    public Interface(){
        this.influxDBClient = XServiceDependencyApplication.context.getBean("influxDBClient", InfluxDBClient.class);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
    }

    public void addServiceTimeNano(double start, double end, double time){

        /* push metric to InfluxDB */
        InterfaceSTNano interfaceSTNano = new InterfaceSTNano();
        interfaceSTNano.interfaceName = this.getName();
        interfaceSTNano.serviceName = this.service.getName();
        interfaceSTNano.time = Instant.ofEpochSecond(
                ((long) end) / 1000000000,
                ((long) end) % 1000000000
        );
        interfaceSTNano.value = time;
        this.influxDBClient.getWriteApiBlocking().writeMeasurement(WritePrecision.NS, interfaceSTNano);
    }

    public void addDownstreamResponseTime(Interface i, double startTime, double endTime, double responseTime){
        String downstreamServiceName = i.getService().getName();
        String downstreamInterfaceName = i.getName();
        if(!this.downstreamInterfaceMap.containsKey(downstreamServiceName)){
            Map<String, Interface> _map = new ConcurrentHashMap<>();
            _map.put(i.getName(), i);
            this.downstreamInterfaceMap.put(downstreamServiceName, _map);
        }else{
            if(!this.downstreamInterfaceMap.get(downstreamServiceName).containsKey(downstreamInterfaceName)){
                this.downstreamInterfaceMap.get(downstreamServiceName).put(downstreamInterfaceName, i);
            }
        }

        /* push metric to InfluxDB */
        InterfaceRTNano interfaceRTNano = new InterfaceRTNano();
        interfaceRTNano.interfaceName = i.getName();
        interfaceRTNano.serviceName = i.getService().getName();
        interfaceRTNano.srcInterfaceName = this.getName();
        interfaceRTNano.srcServiceName = this.getService().getName();
        interfaceRTNano.value = responseTime;
        interfaceRTNano.time = Instant.ofEpochSecond(
                (long) endTime / 1000000000,
                ((long) endTime) % 1000000000
        );
        this.influxDBClient.getWriteApiBlocking().writeMeasurement(WritePrecision.NS, interfaceRTNano);
    }

    /**
     * getDownstreamInterfaceNames
     * @return map serviceName -> InterfaceNames
     */
    public Map<String, Set<String>> getDownstreamInterfaceNames(){
        Map<String, Set<String>> interfaces = new HashMap<>();
        for(String downstreamServiceName : this.downstreamInterfaceMap.keySet()){
            for(String downstreamInterfaceName : this.downstreamInterfaceMap.get(downstreamServiceName).keySet()){
                if(interfaces.containsKey(downstreamServiceName)){
                    interfaces.get(downstreamServiceName).add(downstreamInterfaceName);
                }else{
                    Set<String> interfaceNameSet = new HashSet<>();
                    interfaceNameSet.add(downstreamInterfaceName);
                    interfaces.put(downstreamServiceName, interfaceNameSet);
                }
            }
        }
        return interfaces;
    }
}
