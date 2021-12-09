package com.github.fenrir.xlocalmonitor.rpc;

import com.github.fenrir.xcommon.clients.xlocalmonitor.entities.LocalMonitorOverview;
import com.github.fenrir.xlocalmonitor.services.monitor.XLocalMonitorFactory;
import com.github.fenrir.xmessaging.rpc.annotation.RpcServer;
import oshi.SystemInfo;
import oshi.hardware.GlobalMemory;

import java.util.*;

@RpcServer(name = "genericInfo")
public class GenericInfoServer {

    public GenericInfoServer(){

    }

    public LocalMonitorOverview getOverview(){
        LocalMonitorOverview overview = new LocalMonitorOverview();
        overview.hostname = XLocalMonitorFactory.getHostname();

        List<LocalMonitorOverview.MonitorInstance> monitorInstances = new ArrayList<>();
        for(String key : XLocalMonitorFactory.getMonitorInstanceMap().keySet()){
            LocalMonitorOverview.MonitorInstance monitorInstance = new LocalMonitorOverview.MonitorInstance();
            monitorInstance.name = key;
            monitorInstance.streams = Arrays.asList(XLocalMonitorFactory.getMonitorStreams(key));
            monitorInstance.events = Arrays.asList(XLocalMonitorFactory.getMonitorEvents(key));
            monitorInstance.inspectors = Arrays.asList(XLocalMonitorFactory.getMonitorInspectors(key));
            monitorInstances.add(monitorInstance);
        }

        overview.monitorInstances = monitorInstances;

        Map<String, Integer> cpuInfoMap = this.getProcessorInformation();
        LocalMonitorOverview.CpuInfo cpuInfo = new LocalMonitorOverview.CpuInfo();
        cpuInfo.cpuNum = cpuInfoMap.getOrDefault("cpuNum", -1);
        cpuInfo.socketNum = cpuInfoMap.getOrDefault("socketNum", -1);
        cpuInfo.logicalCpuNum = cpuInfoMap.getOrDefault("logicalCpuNum", -1);
        overview.cpuInfo = cpuInfo;

        Map<String, Long> memInfoMap = this.getMemoryInformation();
        LocalMonitorOverview.MemInfo memInfo = new LocalMonitorOverview.MemInfo();
        memInfo.physicalMemoryNum = memInfoMap.getOrDefault("physicalMemoryNum", (long) -1);
        memInfo.totalMemorySize = memInfoMap.getOrDefault("totalMemorySize", (long) -1);
        overview.memInfo = memInfo;

        return overview;
    }

    public Map<String, Integer> getProcessorInformation(){
        SystemInfo systemInfo = new SystemInfo();
        Map<String, Integer> result = new HashMap<>();

        // RESULT
        result.put("cpuNum", systemInfo.getHardware().getProcessor().getPhysicalProcessorCount());
        result.put("socketNum", systemInfo.getHardware().getProcessor().getPhysicalPackageCount());
        result.put("logicalCpuNum", systemInfo.getHardware().getProcessor().getLogicalProcessorCount());

        return result;
    }

    public Map<String, Long> getMemoryInformation(){
        SystemInfo systemInfo = new SystemInfo();
        GlobalMemory oshiGlobalMemory = systemInfo.getHardware().getMemory();
        Map<String, Long> result = new HashMap<>();

        // RESULT
        result.put("physicalMemoryNum", (long) oshiGlobalMemory.getPhysicalMemory().size());
        result.put("totalMemorySize", oshiGlobalMemory.getTotal());

        return result;
    }
}
