package com.github.fenrir.xservicedependency.services;

import com.github.fenrir.xcommon.utils.Tuple2;
import com.github.fenrir.xcommon.utils.Tuple3;
import com.github.fenrir.xservicedependency.entities.serviceDependency.Interface;
import com.github.fenrir.xservicedependency.entities.trace.OpenTelemetryTraceData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DependencyService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DependencyService.class);

    private Map<String, com.github.fenrir.xservicedependency.entities.serviceDependency.Service> serviceMap = new ConcurrentHashMap<>();

    public void report(OpenTelemetryTraceData traceData){
        for(OpenTelemetryTraceData.ResourceSpan resourceSpan : traceData.resourceSpans){
            for(OpenTelemetryTraceData.InstrumentationLibrarySpan instrumentationLibrarySpan : resourceSpan.instrumentationLibrarySpans){
                for(OpenTelemetryTraceData.Span span : instrumentationLibrarySpan.spans){
                    this.processSpan(span);
                }
            }
        }
    }

    private void processSpan(OpenTelemetryTraceData.Span span){

        String[] splitSpanName = span.name.split(":");
        String serviceName = splitSpanName[0];
        String interfaceName = splitSpanName[1];

        LOGGER.debug("span:");
        LOGGER.debug("\tservice  :{}", serviceName);
        LOGGER.debug("\tinterface:{}", interfaceName);

        Long startUnixTimeNano = Long.valueOf(span.startTimeUnixNano);
        Long endUnixTimeNano = Long.valueOf(span.endTimeUnixNano);
        Long spanTimeNano = endUnixTimeNano - startUnixTimeNano;

        if(!this.serviceMap.containsKey(serviceName)){
            com.github.fenrir.xservicedependency.entities.serviceDependency.Service service =
                    new com.github.fenrir.xservicedependency.entities.serviceDependency.Service();
            service.setName(serviceName);
            this.serviceMap.put(serviceName, service);
        }

        this.serviceMap.get(serviceName).updateInterface(interfaceName, startUnixTimeNano, endUnixTimeNano, spanTimeNano);

        this.processEvent(span);
    }

    private void processEvent(OpenTelemetryTraceData.Span span){
        Map<String, Tuple2<Long, Long>> eventMap = new HashMap<>();
        String serviceName = span.name.split(":")[0];
        if(span.events == null){
            return;
        }
        for(OpenTelemetryTraceData.Event event : span.events){
            String[] splitEventName = event.name.split(":");
            String downstreamServiceName = splitEventName[0];
            String action = splitEventName[1];
            String uuid = splitEventName[2];
            String downstreamInterfaceName = splitEventName[3];

            String label = downstreamServiceName + ":" + downstreamInterfaceName + ":" + uuid;

            if(eventMap.containsKey(label)){
                if(action.equals("start")){
                    if(eventMap.get(label).second != -1){
                        this.serviceMap.get(serviceName).updateInterfaceDownstreamInterface(
                                span.name.split(":")[1],
                                this.serviceMap.getOrDefault(label.split(":")[0], null),
                                downstreamInterfaceName,
                                Long.parseLong(event.timeUnixNano),
                                eventMap.get(label).second,
                                eventMap.get(label).second - Long.parseLong(event.timeUnixNano)
                        );
                    }
                }else{
                    if(eventMap.get(label).first != -1){
                        this.serviceMap.get(serviceName).updateInterfaceDownstreamInterface(
                                span.name.split(":")[1],
                                this.serviceMap.getOrDefault(label.split(":")[0], null),
                                downstreamInterfaceName,
                                eventMap.get(label).first,
                                Long.parseLong(event.timeUnixNano),
                                Long.parseLong(event.timeUnixNano) - eventMap.get(label).first
                        );
                    }
                }
            }else{
                eventMap.put(label, action.equals("start") ?
                        new Tuple2<>(Long.valueOf(event.timeUnixNano), (long) -1) :
                        new Tuple2<>((long) -1, Long.valueOf(event.timeUnixNano)));
            }
        }
    }

    public Map<String, com.github.fenrir.xservicedependency.entities.serviceDependency.Service> getServiceMap() {
        return serviceMap;
    }

    public void setServiceMap(Map<String, com.github.fenrir.xservicedependency.entities.serviceDependency.Service> serviceMap) {
        this.serviceMap = serviceMap;
    }

    public Set<String> getServiceInterfaceNames(String serviceName){
        com.github.fenrir.xservicedependency.entities.serviceDependency.Service srv = this.getServiceMap().getOrDefault(serviceName, null);
        if(srv != null){
            return srv.getInterfaceMap().keySet();
        }
        return null;
    }

    public Set<String> getServiceInterfaceDownstreamInterfaceNames(String serviceName, String interfaceName){
        com.github.fenrir.xservicedependency.entities.serviceDependency.Service srv = this.getServiceMap().getOrDefault(serviceName, null);
        if(srv != null){
            Interface i = srv.getInterface(interfaceName);
            if(i != null){
                return i.getDownstreamInterfaceNames();
            }
        }
        return null;
    }

    public Map<String, Map<String, Tuple2<Interface, LinkedList<Tuple3<Long, Long, Long>>>>> getDownstreamInterfaceMap(String serviceName, String interfaceName){
        com.github.fenrir.xservicedependency.entities.serviceDependency.Service srv = this.getServiceMap().getOrDefault(serviceName, null);
        if(srv != null){
            Interface i = srv.getInterface(interfaceName);
            if(i != null){
                return i.getDownstreamInterfaceMap();
            }
        }
        return null;
    }
}
