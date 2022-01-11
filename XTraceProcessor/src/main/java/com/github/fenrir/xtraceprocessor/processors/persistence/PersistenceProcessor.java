package com.github.fenrir.xtraceprocessor.processors.persistence;

import com.github.fenrir.xcommon.utils.Tuple2;
import com.github.fenrir.xtraceprocessor.entities.serviceDependency.SubCall;
import com.github.fenrir.xtraceprocessor.entities.serviceDependency.Span;
import com.github.fenrir.xtraceprocessor.processors.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class PersistenceProcessor extends Processor {
    static private final Logger LOGGER = LoggerFactory.getLogger(PersistenceProcessor.class);

    private Map<String, Service> serviceMap = new ConcurrentHashMap<>();

    @Override
    protected Span internalDoProcess(Span span) {
        if(span == null) return null;

        processSpan(span);
        return span;
    }

    private void processSpan(Span span){

        String serviceName = span.getServiceName();
        String interfaceName = span.getInterfaceName();

        LOGGER.debug("span:");
        LOGGER.debug("\tservice  :{}", serviceName);
        LOGGER.debug("\tinterface:{}", interfaceName);

        double startTimeNano = span.getStartTimeNano();
        double endTimeNano = span.getEndTimeNano();
        double spanTimeNano = endTimeNano - startTimeNano;

        if(!this.serviceMap.containsKey(serviceName)){
            Service service =
                    new Service();
            service.setName(serviceName);
            this.serviceMap.put(serviceName, service);
        }

        this.serviceMap.get(serviceName).updateInterface(interfaceName, startTimeNano, endTimeNano, spanTimeNano);

        this.processEvent(span);
    }

    private void processEvent(Span span){
        Map<String, Tuple2<Long, Long>> eventMap = new HashMap<>();
        String serviceName = span.getServiceName();
        String interfaceName = span.getInterfaceName();

        Iterator<SubCall> eventIterator = span.getEventsIterator();

        while(eventIterator.hasNext()){
            SubCall subCall = eventIterator.next();

            String downstreamServiceName = subCall.getServiceName();
            String downstreamInterfaceName = subCall.getInterfaceName();

            this.serviceMap.get(serviceName).updateDownstreamInterface(
                    interfaceName,
                    this.serviceMap.getOrDefault(downstreamServiceName, null),
                    downstreamInterfaceName,
                    subCall.getStartTimeNano(),
                    subCall.getEndTimeNano(),
                    subCall.getEndTimeNano() - subCall.getStartTimeNano()
            );
        }
    }

    public Set<String> getServiceNames(){
        return this.serviceMap.keySet();
    }

    private Map<String, com.github.fenrir.xtraceprocessor.processors.persistence.Service> getServiceMap() {
        return serviceMap;
    }

    public Set<String> getServiceInterfaceNames(String serviceName){
        com.github.fenrir.xtraceprocessor.processors.persistence.Service srv = this.getServiceMap().getOrDefault(serviceName, null);
        if(srv != null){
            return srv.getInterfaceMap().keySet();
        }
        return null;
    }

    public Map<String, Set<String>> getDownstreamInterfaceNames(String serviceName, String interfaceName){
        com.github.fenrir.xtraceprocessor.processors.persistence.Service srv = this.getServiceMap().getOrDefault(serviceName, null);
        if(srv != null){
            Interface i = srv.getInterface(interfaceName);
            if(i != null){
                return i.getDownstreamInterfaceNames();
            }
        }
        return null;
    }
}
