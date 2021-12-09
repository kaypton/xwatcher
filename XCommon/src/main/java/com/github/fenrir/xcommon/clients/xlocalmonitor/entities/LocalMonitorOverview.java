package com.github.fenrir.xcommon.clients.xlocalmonitor.entities;

import java.util.List;

/**
 * {
 *     "hostname": "compute1",
 *     "monitorInstances": [
 *          "xxx": {
 *              "streams": ["xxx", "xxx", ...],
 *              "events": ["xxx", "xxx", ...],
 *              "inspectors": ["xxx", ...]
 *          }
 *     ],
 *     "cpuInfo": {
 *          "cpuNum": 1,
 *          "socketNum": 1,
 *          "logicalCpuNum": 1
 *     },
 *     "mem_info": {
 *         "physicalMemorySize": 1000,
 *         "totalMemorySize" : 1000
 *     }
 * }
 */
public class LocalMonitorOverview {
    public static class MonitorInstance {
        public String name;
        public List<String> streams;
        public List<String> events;
        public List<String> inspectors;
    }
    public static class CpuInfo {
        public int cpuNum;
        public int socketNum;
        public int logicalCpuNum;
    }
    public static class MemInfo {
        public long physicalMemoryNum;
        public long totalMemorySize;
    }
    public String hostname;
    public List<MonitorInstance> monitorInstances;
    public CpuInfo cpuInfo;
    public MemInfo memInfo;

    public LocalMonitorOverview() {}
}
