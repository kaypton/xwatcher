package com.github.fenrir.xlocalmonitor.monitors.local;

import com.github.fenrir.xcommon.utils.CommonUtils;
import com.github.fenrir.xcommon.utils.Tuple2;
import com.github.fenrir.xlocalmonitor.annotations.Monitor;
import com.github.fenrir.xlocalmonitor.entities.MessageBuilder;
import com.github.fenrir.xlocalmonitor.inspectors.local.memory.linux.MemInfo;
import com.github.fenrir.xlocalmonitor.monitors.BaseMonitor;
import com.github.fenrir.xlocalmonitor.services.prometheus.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@Monitor(name = "MemoryMonitor",
        streams = {"system.ram"},
        inspectors = {"linux.meminfo"})
public class MemoryMonitor extends BaseMonitor {
    private static final Logger logger = LoggerFactory.getLogger("MemoryMonitor");

    private MemInfo memInfo;
    private Map<String, Map<String, Object>> lastMetric;

    private static class MemoryMonitorTimerTask extends TimerTask {

        private final MemInfo memInfo;
        private final MemoryMonitor monitor;

        public MemoryMonitorTimerTask(MemInfo memInfo,
                                      MemoryMonitor monitor){
            this.memInfo = memInfo;
            this.monitor = monitor;
        }

        @Override
        public void run(){
            this.memInfo.refresh();

            Tuple2<Long, MemInfo.Unit> memTotal = memInfo.getValue("MemTotal");
            Tuple2<Long, MemInfo.Unit> memFree = memInfo.getValue("MemFree");
            Tuple2<Long, MemInfo.Unit> memAvailable = memInfo.getValue("MemAvailable");

            try{
                Map<String, Object> systemRamMsg = MessageBuilder.builder("Stream", "system.ram")
                        .withValue("timestamp", CommonUtils.getTimestamp(), Long.class)
                        .withObject("hostInfo")  /* HostInfo */
                        .withValue("host", this.monitor.getHostname(), String.class)
                        .withValue("monitorId", this.monitor.getUuid(), String.class)
                        .buildObject()           /* end HostInfo */
                        .withObject("value")     /* SystemRamStat */
                        .withValue("mem.total.KiB", memTotal.first, Long.class)
                        .withValue("mem.free.KiB", memFree.first, Long.class)
                        .withValue("mem.available.KiB", memAvailable.first, Long.class)
                        .buildObject()           /* end SystemRamStat */
                        .build();
                this.monitor.sendStreamData("system.ram", systemRamMsg);
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    /**
     * {
     *     "system.ram": [(MemTotal), (MemFree), (MemAvailable)]
     * }
     * @return Map
     */
    @Override
    public Map<String, Map<String, Object>> extract(){
        logger.info("[RPC][extract] start");

        Map<String, Map<String, Object>> ret = new HashMap<>();
        MemInfo memInfo = (MemInfo) this.apiMap.get("linux.meminfo");
        memInfo.refresh();

        Tuple2<Long, MemInfo.Unit> memTotal = memInfo.getValue("MemTotal");
        Tuple2<Long, MemInfo.Unit> memFree = memInfo.getValue("MemFree");
        Tuple2<Long, MemInfo.Unit> memAvailable = memInfo.getValue("MemAvailable");

        logger.info("[RPC][extract] MemTotal: {}, MemFree: {}, MemAvailable: {} " +
                        "Timestamp: {}",
                memTotal.first, memFree.first, memAvailable.first, CommonUtils.getTimestamp());
        try{
            ret.put("system.ram", MessageBuilder.builder("Stream", "system.ram")
                    .withValue("timestamp", CommonUtils.getTimestamp(), Long.class)
                    .withObject("hostInfo")  /* HostInfo */
                    .withValue("host", this.getHostname(), String.class)
                    .withValue("monitorId", this.getUuid(), String.class)
                    .buildObject()           /* end HostInfo */
                    .withObject("value")      /* SystemRamStat */
                    .withValue("mem.total.KiB", memTotal.first, Long.class)
                    .withValue("mem.free.KiB", memFree.first, Long.class)
                    .withValue("mem.available.KiB", memAvailable.first, Long.class)
                    .buildObject()           /* end SystemRamStat */
                    .build());
        } catch (Exception e){
            e.printStackTrace();
        }

        logger.info("[RPC][extract] result: {}", ret);
        return ret;
    }

    @Override
    protected void postStart() {

    }

    @Override
    protected void doStart() {
        this.registerTimerTask(new MemoryMonitorTimerTask(this.memInfo, this),
                (long) 1000);
    }

    @Override
    protected void preStart() {
        this.memInfo = (MemInfo) this.getApiMap().get("linux.meminfo");

        DataContainer dataContainer = new DataContainer("MemoryMonitor");

        Data data = new Data("system_ram_total_kib",
                this.getDataMethod(),
                "ram total size in kib",
                MetricType.GAUGE,
                "system_ram_total_kib");

        data.addLabel("nodeId", this.getUuid());
        data.addLabel("hostname", this.getHostname());

        dataContainer.registerData(data);

        data = new Data("system_ram_free_kib",
                this.getDataMethod(),
                "ram free in kib",
                MetricType.GAUGE,
                "system_ram_free_kib");

        data.addLabel("nodeId", this.getUuid());
        data.addLabel("hostname", this.getHostname());

        dataContainer.registerData(data);

        data = new Data("system_ram_available_kib",
                this.getDataMethod(),
                "ram available in free",
                MetricType.GAUGE,
                "system_ram_available_kib");

        data.addLabel("nodeId", this.getUuid());
        data.addLabel("hostname", this.getHostname());

        dataContainer.registerData(data);

        DataContainerService.registerDataContainer(dataContainer);
    }

    @Override
    public void doStop() {

    }

    public GetDataMethod getDataMethod(){
        return (metricName, params) -> {

            MemInfo memInfo = (MemInfo) this.apiMap.get("linux.meminfo");
            memInfo.refresh();

            Tuple2<Long, MemInfo.Unit> memTotal = memInfo.getValue("MemTotal");
            Tuple2<Long, MemInfo.Unit> memFree = memInfo.getValue("MemFree");
            Tuple2<Long, MemInfo.Unit> memAvailable = memInfo.getValue("MemAvailable");

            switch (metricName) {
                case "system_ram_total_kib":
                    return (double) memTotal.first;
                case "system_ram_free_kib":
                    return (double) memFree.first;
                case "system_ram_available_kib":
                    return (double) memAvailable.first;
            }
            return null;
        };
    }
}
