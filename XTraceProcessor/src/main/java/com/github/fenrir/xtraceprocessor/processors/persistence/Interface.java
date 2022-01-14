package com.github.fenrir.xtraceprocessor.processors.persistence;

import com.github.fenrir.xtraceprocessor.XTraceProcessorApplication;
import com.github.fenrir.xtraceprocessor.processors.persistence.entities.influxDB.InterfaceRTNano;
import com.github.fenrir.xtraceprocessor.processors.persistence.entities.influxDB.InterfaceRTNanoWithoutSrc;
import com.github.fenrir.xtraceprocessor.processors.persistence.entities.influxDB.InterfaceSTNano;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.domain.WritePrecision;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Interface {
    private static final Logger LOGGER = LoggerFactory.getLogger(Interface.class);

    public static final String version = "v0.0.2";
    /**
     * interface name
     */
    private String name;

    private String uri;

    /**
     * the service which contains the interface
     */
    private Service service;

    // service name -> tuple(interface object, response time list)
    // tuple(startTime:Long, endTime:Long, responseTime:Long)
    private final Map<String, Map<String, Interface>> downstreamInterfaceMap =
            new ConcurrentHashMap<>();

    private InfluxDBClient influxDBClient;

    private final ThreadPoolExecutor influxDBPushExecutor = new ThreadPoolExecutor(
            10000, 100000, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<>()
    );

    public Interface(){
        this.influxDBClient = XTraceProcessorApplication.context.getBean("influxDBClient", InfluxDBClient.class);
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

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public void addServiceTimeNano(double start, double end, double time){
        LOGGER.info("addServiceTimeNano push active thread: {}", influxDBPushExecutor.getActiveCount());

        /* push metric to InfluxDB */
        InterfaceSTNano interfaceSTNano = new InterfaceSTNano();
        interfaceSTNano.interfaceName = this.getName();
        interfaceSTNano.serviceName = this.service.getName();
        interfaceSTNano.interfaceURI = this.getUri();
        interfaceSTNano.version = Interface.version;
        interfaceSTNano.time = Instant.ofEpochSecond(
                ((long) end) / 1000000000,
                ((long) end) % 1000000000
        );
        interfaceSTNano.value = time;

        this.influxDBPushExecutor.submit(() -> {
            this.influxDBClient.getWriteApiBlocking().writeMeasurement(WritePrecision.NS, interfaceSTNano);
        });
    }

    public void addDownstreamResponseTime(Interface i, double startTime, double endTime, double responseTime){
        LOGGER.info("addDownstreamResponseTime push active thread: {}", influxDBPushExecutor.getActiveCount());
        String downstreamServiceName = i.getService().getName();
        String downstreamInterfaceName = i.getName();
        synchronized (this.downstreamInterfaceMap) {
            if(!this.downstreamInterfaceMap.containsKey(downstreamServiceName)){
                Map<String, Interface> _map = new ConcurrentHashMap<>();
                _map.put(i.getName(), i);
                this.downstreamInterfaceMap.put(downstreamServiceName, _map);
            }else{
                if(!this.downstreamInterfaceMap.get(downstreamServiceName).containsKey(downstreamInterfaceName)){
                    this.downstreamInterfaceMap.get(downstreamServiceName).put(downstreamInterfaceName, i);
                }
            }
        }

        /* push metric to InfluxDB */
        InterfaceRTNano interfaceRTNano = new InterfaceRTNano();
        interfaceRTNano.interfaceName = i.getName();
        interfaceRTNano.serviceName = i.getService().getName();
        interfaceRTNano.srcInterfaceName = this.getName();
        interfaceRTNano.srcServiceName = this.getService().getName();
        interfaceRTNano.interfaceURI = this.getUri();
        interfaceRTNano.srcInterfaceURI = i.getUri();
        interfaceRTNano.value = responseTime;
        interfaceRTNano.version = Interface.version;
        interfaceRTNano.time = Instant.ofEpochSecond(
                (long) endTime / 1000000000,
                ((long) endTime) % 1000000000
        );

        InterfaceRTNanoWithoutSrc interfaceRTNanoWithoutSrc = new InterfaceRTNanoWithoutSrc();
        interfaceRTNanoWithoutSrc.interfaceName = i.getName();
        interfaceRTNanoWithoutSrc.serviceName = i.getService().getName();
        interfaceRTNanoWithoutSrc.interfaceURI = i.getUri();
        interfaceRTNanoWithoutSrc.version = Interface.version;
        interfaceRTNanoWithoutSrc.value = responseTime;
        interfaceRTNano.time = Instant.ofEpochSecond(
                (long) endTime / 1000000000,
                ((long) endTime) % 1000000000
        );

        this.influxDBPushExecutor.submit(() -> {
            this.influxDBClient.getWriteApiBlocking().writeMeasurement(WritePrecision.NS, interfaceRTNano);
            this.influxDBClient.getWriteApiBlocking().writeMeasurement(WritePrecision.NS, interfaceRTNanoWithoutSrc);
        });
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
