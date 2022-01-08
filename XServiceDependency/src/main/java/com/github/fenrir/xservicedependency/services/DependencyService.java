package com.github.fenrir.xservicedependency.services;

import com.github.fenrir.xservicedependency.processors.anomalyDetection.AnomalyDetectionProcessor;
import com.github.fenrir.xservicedependency.processors.persistence.PersistenceProcessor;
import com.github.fenrir.xservicedependency.entities.serviceDependency.Span;
import com.github.fenrir.xservicedependency.entities.trace.OpenTelemetryTraceData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DependencyService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DependencyService.class);

    private final PersistenceProcessor persistenceFilter;

    public DependencyService(){
        this.persistenceFilter = new PersistenceProcessor();
        this.persistenceFilter.setNextFilter(new AnomalyDetectionProcessor());
    }

    public void reportOtelTraceData(OpenTelemetryTraceData traceData){
        for(OpenTelemetryTraceData.ResourceSpan resourceSpan : traceData.resourceSpans){
            for(OpenTelemetryTraceData.InstrumentationLibrarySpan instrumentationLibrarySpan : resourceSpan.instrumentationLibrarySpans){
                for(OpenTelemetryTraceData.Span span : instrumentationLibrarySpan.spans){
                    this.persistenceFilter.doFilter(Span.create(span));
                }
            }
        }
    }

    public Set<String> getServiceNames(){
        return this.persistenceFilter.getServiceNames();
    }

    public Set<String> getServiceInterfaceNames(String serviceName){
        return this.persistenceFilter.getServiceInterfaceNames(serviceName);
    }

    public Map<String, Set<String>> getDownstreamInterfaceNames(String serviceName, String interfaceName){
        return this.persistenceFilter.getDownstreamInterfaceNames(serviceName, interfaceName);
    }
}
