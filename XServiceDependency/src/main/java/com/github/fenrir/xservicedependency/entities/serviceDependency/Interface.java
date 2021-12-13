package com.github.fenrir.xservicedependency.entities.serviceDependency;

import com.github.fenrir.xcommon.utils.Tuple2;
import com.github.fenrir.xcommon.utils.Tuple3;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Interface {
    // private static final Logger LOGGER = LoggerFactory.getLogger(Interface.class);

    static private final int maxServiceTimeNanoListSize = 10000;
    static private final int maxResponseTimeNanoListSize = 10000;

    private String name;
    private Service service;
    // tuple startUnixTimeNano, endUnixTimeNano, serviceTimeNano
    private final LinkedList<Tuple3<Long, Long, Long>> serviceTimeNanoList = new LinkedList<>();

    // service name -> tuple(interface object, response time list)
    // tuple startTime, endTime, responseTime
    private final Map<String, Map<String, Tuple2<Interface, LinkedList<Tuple3<Long, Long, Long>>>>> downstreamInterfaceMap = new ConcurrentHashMap<>();

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

    public void addServiceTimeNano(Long start, Long end, Long time){
        if(this.serviceTimeNanoList.size() >= maxServiceTimeNanoListSize){
            this.serviceTimeNanoList.removeFirst();
        }
        this.serviceTimeNanoList.addLast(new Tuple3<>(start, end, time));
    }

    public void addDownstreamResponseTime(Interface i, Long startTime, Long endTime, Long responseTime){
        String downstreamServiceName = i.getService().getName();
        if(!this.downstreamInterfaceMap.containsKey(i.getService().getName())){
            Tuple2<Interface, LinkedList<Tuple3<Long, Long, Long>>> tuple = new Tuple2<>(i, new LinkedList<>());
            Map<String, Tuple2<Interface, LinkedList<Tuple3<Long, Long, Long>>>> _map = new ConcurrentHashMap<>();
            _map.put(i.getName(), tuple);
            this.downstreamInterfaceMap.put(downstreamServiceName, _map);
        }else{
            if(!this.downstreamInterfaceMap.get(i.getService().getName()).containsKey(i.getName())){
                Tuple2<Interface, LinkedList<Tuple3<Long, Long, Long>>> tuple = new Tuple2<>(i, new LinkedList<>());
                this.downstreamInterfaceMap.get(downstreamServiceName).put(i.getName(), tuple);
            }
        }

        if(this.downstreamInterfaceMap.get(downstreamServiceName).get(i.getName()).second.size() >= maxResponseTimeNanoListSize){
            this.downstreamInterfaceMap.get(downstreamServiceName).get(i.getName()).second.removeFirst();
        }
        this.downstreamInterfaceMap.get(downstreamServiceName).get(i.getName()).second.addLast(new Tuple3<>(startTime, endTime, responseTime));
    }

    /**
     * getDownstreamInterfaceNames
     * @return string = "serviceName:interfaceName"
     */
    public Set<String> getDownstreamInterfaceNames(){
        Set<String> interfaces = new HashSet<>();
        for(String downstreamService : this.downstreamInterfaceMap.keySet()){
            for(String downstreamInterfaceName : this.downstreamInterfaceMap.get(downstreamService).keySet()){
                interfaces.add(downstreamService + ":" + downstreamInterfaceName);
            }
        }
        return interfaces;
    }

    public Map<String, Map<String, Tuple2<Interface, LinkedList<Tuple3<Long, Long, Long>>>>> getDownstreamInterfaceMap(){
        return this.downstreamInterfaceMap;
    }

    public LinkedList<Tuple3<Long, Long, Long>> getServiceTimeNanoList() {
        return serviceTimeNanoList;
    }
}
