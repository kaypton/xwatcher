package com.github.fenrir.xtraceprocessor.processors.persistence;

import com.github.fenrir.xtraceprocessor.configs.URISelectorConfig;
import com.github.fenrir.xtraceprocessor.entities.trace.SubCall;
import com.github.fenrir.xtraceprocessor.entities.trace.Span;
import com.github.fenrir.xtraceprocessor.processors.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class PersistenceProcessor extends Processor {
    static private final Logger LOGGER = LoggerFactory.getLogger(PersistenceProcessor.class);

    private Map<String, Service> serviceMap = new ConcurrentHashMap<>();

    private final URISelectorConfig selectorConfig;

    public PersistenceProcessor(){
        this.selectorConfig = URISelectorConfig.tmpCreate();
    }

    @Override
    protected Span internalDoProcess(Span span) {
        if(span == null) return null;

        processSpan(span);
        return span;
    }

    private void processSpan(Span span){

        String serviceName = span.getServiceName();
        String interfaceName = span.getInterfaceName();

        double startTimeNano = span.getStartTimeNano();
        double endTimeNano = span.getEndTimeNano();
        double spanTimeNano = endTimeNano - startTimeNano;

        if(!this.serviceMap.containsKey(serviceName)){
            Service service = new Service(this.selectorConfig);
            service.setName(serviceName);
            this.serviceMap.put(serviceName, service);
        }

        this.serviceMap.get(serviceName).updateInterface(
                interfaceName, span.getInterfaceURI(), startTimeNano, endTimeNano, spanTimeNano);

        this.processSubCalls(span);
    }

    private void processSubCalls(Span span){
        String serviceName = span.getServiceName();
        String interfaceName = span.getInterfaceName();
        String interfaceURI = span.getInterfaceURI();

        Iterator<SubCall> eventIterator = span.getEventsIterator();

        while(eventIterator.hasNext()){
            SubCall subCall = eventIterator.next();

            String downstreamServiceName = subCall.getServiceName();
            String downstreamInterfaceName = subCall.getInterfaceName();
            String downstreamInterfaceURI = subCall.getInterfaceURI();

            this.serviceMap.get(serviceName).updateDownstreamInterface(
                    interfaceName,
                    interfaceURI,
                    this.serviceMap.getOrDefault(downstreamServiceName, null),
                    downstreamInterfaceName,
                    downstreamInterfaceURI,
                    subCall.getStartTimeNano(),
                    subCall.getEndTimeNano(),
                    subCall.getEndTimeNano() - subCall.getStartTimeNano()
            );
        }
    }

    public Set<String> getServiceNames(){
        return this.serviceMap.keySet();
    }

    public Set<String> getServiceInterfaceNames(String serviceName){
        com.github.fenrir.xtraceprocessor.processors.persistence.Service srv = this.serviceMap.getOrDefault(serviceName, null);
        if(srv != null){
            return srv.getInterfaceMap().keySet();
        }
        return null;
    }

    public Map<String, Set<String>> getDownstreamInterfaceNames(String serviceName, String interfaceName){
        com.github.fenrir.xtraceprocessor.processors.persistence.Service srv = this.serviceMap.getOrDefault(serviceName, null);
        if(srv != null){
            Interface i = srv.getInterface(interfaceName);
            if(i != null){
                return i.getDownstreamInterfaceNames();
            }
        }
        return null;
    }
}
