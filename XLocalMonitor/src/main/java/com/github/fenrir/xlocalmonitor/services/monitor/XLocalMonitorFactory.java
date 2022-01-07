package com.github.fenrir.xlocalmonitor.services.monitor;

import com.github.fenrir.xlocalmonitor.annotations.Inspector;
import com.github.fenrir.xlocalmonitor.annotations.InspectorScan;
import com.github.fenrir.xlocalmonitor.annotations.Monitor;
import com.github.fenrir.xlocalmonitor.annotations.MonitorScan;
import com.github.fenrir.xlocalmonitor.monitors.BaseMonitor;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class XLocalMonitorFactory {
    private static final Logger logger = LoggerFactory.getLogger(
            "XLocalMonitorFactory");

    static private final Map<String, BaseMonitor> monitorInstanceMap = new ConcurrentHashMap<>();
    static private final Map<String, String[]> monitorStreamMap = new ConcurrentHashMap<>();
    static private final Map<String, String[]> monitorEventMap = new ConcurrentHashMap<>();
    static private final Map<String, String[]> monitorInspectorMap = new ConcurrentHashMap<>();

    static private String _hostname;

    static public String getHostname(){
        return _hostname;
    }

    static private final Map<String, Class<?>> inspectorMap = new ConcurrentHashMap<>();

    static public void init(Class<?> anyClazz, String hostname, String uuid){
        _hostname = hostname;
        MonitorScan scan = anyClazz.getDeclaredAnnotation(MonitorScan.class);
        String[] monitorScanPath = scan.path();

        InspectorScan inspectorScan = anyClazz.getDeclaredAnnotation(InspectorScan.class);
        String[] inspectorScanPath = inspectorScan.path();

        // scan inspectors
        for(String path : inspectorScanPath){
            Reflections reflections = new Reflections(path);
            Set<Class<?>> inspectorClazz = reflections.getTypesAnnotatedWith(Inspector.class);
            for(Class<?> inspectorClass : inspectorClazz){
                Inspector inspectorAnnotation = inspectorClass.getDeclaredAnnotation(Inspector.class);
                String inspectorName = inspectorAnnotation.name();
                inspectorMap.put(inspectorName, inspectorClass);
                logger.info("Scan inspector " + inspectorName);
            }
        }

        // scan monitors
        for(String path : monitorScanPath){
            Reflections reflections = new Reflections(path);
            Set<Class<?>> monitorClazz = reflections.getTypesAnnotatedWith(Monitor.class);
            for(Class<?> monitorClass : monitorClazz){
                Monitor monitorAnnotation = monitorClass.getDeclaredAnnotation(Monitor.class);
                String monitorName = monitorAnnotation.name();

                monitorStreamMap.put(monitorName, monitorAnnotation.streams());        // streams
                monitorEventMap.put(monitorName, monitorAnnotation.events());          // events
                monitorInspectorMap.put(monitorName, monitorAnnotation.inspectors());  // inspectors

                logger.info("Scan monitor " + monitorName);
                Map<String, Object> _inspectorMap = new HashMap<>();

                // inject inspector
                for(String inspectorName : monitorAnnotation.inspectors()){
                    if(!inspectorMap.containsKey(inspectorName))
                        logger.error("inspector " + inspectorName + " do not exist");
                    else {
                        try{
                            // instantiation required inspector
                            // every time we create a new inspector instance
                            Object inspectorInstance = inspectorMap.get(inspectorName).getDeclaredConstructor().newInstance();
                            _inspectorMap.put(inspectorName, inspectorInstance);
                        } catch (InstantiationException
                                | IllegalAccessException
                                | InvocationTargetException
                                | NoSuchMethodException e) {
                            e.printStackTrace();
                        }
                    }
                }

                try {
                    BaseMonitor monitorInstance = (BaseMonitor) monitorClass.getDeclaredConstructor().newInstance();

                    monitorInstance.setHostname(hostname);
                    monitorInstance.setUuid(uuid);
                    monitorInstance.setApiMap(_inspectorMap);

                    String[] streams = monitorAnnotation.streams();
                    String[] events = monitorAnnotation.events();

                    // register events and streams
                    for(String stream : streams){
                        monitorInstance.registerStream(stream);
                    }
                    for(String event : events){
                        monitorInstance.registerEvent(event);
                    }
                    monitorInstanceMap.put(monitorName, monitorInstance);
                } catch (InstantiationException
                        | IllegalAccessException
                        | InvocationTargetException
                        | NoSuchMethodException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    static public BaseMonitor getMonitorInstanceFromName(String monitorName){
        return monitorInstanceMap.get(monitorName);
    }

    static public Object getInspectorFromName(String inspectorName){
        return inspectorMap.get(inspectorName);
    }

    static public Map<String, Class<?>> getInspectorMap(){
        return inspectorMap;
    }

    static public Map<String, BaseMonitor> getMonitorInstanceMap(){
        return monitorInstanceMap;
    }

    static public String[] getMonitorEvents(String monitorName){
        return monitorEventMap.getOrDefault(monitorName, null);
    }

    static public String[] getMonitorStreams(String monitorName){
        return monitorStreamMap.getOrDefault(monitorName, null);
    }

    static public String[] getMonitorInspectors(String monitorName){
        return monitorInspectorMap.getOrDefault(monitorName, null);
    }
}
