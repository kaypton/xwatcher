package com.github.fenrir.xlocalmonitor.monitors.local;

import com.github.fenrir.xcommon.utils.CommonUtils;
import com.github.fenrir.xlocalmonitor.annotations.Monitor;
import com.github.fenrir.xlocalmonitor.entities.MessageBuilder;
import com.github.fenrir.xlocalmonitor.inspectors.local.cpu.linux.proc.Stat;
import com.github.fenrir.xlocalmonitor.monitors.BaseMonitor;
import com.github.fenrir.xlocalmonitor.services.prometheus.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

@Monitor(name = "CpuUsageMonitor",
        streams = {"system.cpu.system.usage",
                   "system.cpu.user.usage",
                   "system.cpu.total.usage",
                   "system.cpu.usage"},
        inspectors = {"linux.stat"})
public class CpuUsageMonitor extends BaseMonitor {

    private Stat stat;
    private final Map<String, Map<String, Object>> lastMetric = new ConcurrentHashMap<>();

    private static class CpuUsageMonitorTimerTask extends TimerTask {

        @Getter @Setter private Stat stat;
        @Getter @Setter private CpuUsageMonitor monitor;
        @Getter @Setter private Map<String, Map<String, Object>> lastMetric;

        @Getter @Setter private Stat.StatData data = null;

        public CpuUsageMonitorTimerTask(Stat stat,
                                        CpuUsageMonitor monitor,
                                        Map<String, Map<String, Object>> lastMetric){
            this.lastMetric = lastMetric;
            this.setStat(stat);
            this.setMonitor(monitor);
        }

        @Override
        public void run(){
            if(this.data == null){
                /*
                * We need at least two `cpu time' sample (two Stat.StatData)
                * to calculate the cpu usage. the field data is null
                * when the CpuUsageMonitorTimerTask class is created at the first time,
                * so we should just get one sample at first and do no calculation
                * */
                this.data = this.stat.get();
            }else{
                Stat.StatData nowStatData = this.stat.get();

                Long preTotalTime = this.data.getTotalTime();
                Long preIdleTime = this.data.getCpuIdleTime();
                Long preUserTime = this.data.getCpuUserTime();
                Long preSystemTime = this.data.getCpuSystemTime();

                Long nowTotalTime = nowStatData.getTotalTime();
                Long nowIdleTime = nowStatData.getCpuIdleTime();
                Long nowUserTime = nowStatData.getCpuUserTime();
                Long nowSystemTime = nowStatData.getCpuSystemTime();

                long totalInterval = nowTotalTime - preTotalTime;
                long idleInterval = nowIdleTime - preIdleTime;
                long userInterval = nowUserTime - preUserTime;
                long systemInterval = nowSystemTime - preSystemTime;

                Double totalUsage = ((double) totalInterval - (double) idleInterval) / (double) totalInterval;
                Double userUsage = (double) userInterval / (double) totalInterval;
                Double systemUsage = (double) systemInterval / (double) totalInterval;

                this.data = nowStatData;

                try{
                    Map<String, Object> totalUsageMsg = MessageBuilder.builder("Stream", "system.cpu.total.usage")
                            .withValue("value", totalUsage, Double.class)
                            .withValue("timestamp", CommonUtils.getTimestampMs(), Long.class)
                            .withObject("hostInfo")   /* HostInfo */
                            .withValue("host", this.getMonitor().getHostname(), String.class)
                            .withValue("monitorId", this.getMonitor().getUuid(), String.class)
                            .buildObject()            /* end HostInfo */
                            .build();

                    Map<String, Object> userUsageMsg = MessageBuilder.builder("Stream", "system.cpu.user.usage")
                            .withValue("value", userUsage, Double.class)
                            .withValue("timestamp", CommonUtils.getTimestampMs(), Long.class)
                            .withObject("hostInfo")   /* HostInfo */
                            .withValue("host", this.getMonitor().getHostname(), String.class)
                            .withValue("monitorId", this.getMonitor().getUuid(), String.class)
                            .buildObject()            /* end HostInfo */
                            .build();
                    Map<String, Object> systemUsageMsg = MessageBuilder.builder("Stream", "system.cpu.system.usage")
                            .withValue("value", systemUsage, Double.class)
                            .withValue("timestamp", CommonUtils.getTimestampMs(), Long.class)
                            .withObject("hostInfo")   /* HostInfo */
                            .withValue("host", this.getMonitor().getHostname(), String.class)
                            .withValue("monitorId", this.getMonitor().getUuid(), String.class)
                            .buildObject()            /* end HostInfo */
                            .build();
                    Map<String, Object> cpuUsageMsg = MessageBuilder.builder("Stream", "system.cpu.usage")
                            .withObject("hostInfo")    /* HostInfo */
                            .withValue("host", this.getMonitor().getHostname(), String.class)
                            .withValue("monitorId", this.getMonitor().getUuid(), String.class)
                            .buildObject()            /* end HostInfo */
                            .withObject("value")   /* SystemCpuStat */
                            .withValue("cpu.core.num", this.data.getCoreNum(), Integer.class)
                            .withValue("cpu.system.usage", systemUsage, Double.class)
                            .withValue("cpu.user.usage", userUsage, Double.class)
                            .withValue("cpu.total.usage", totalUsage, Double.class)
                            .buildObject()     /* end SystemCpuStat */
                            .withValue("timestamp", CommonUtils.getTimestampMs(), Long.class)
                            .build();

                    this.getMonitor().sendStreamData("system.cpu.total.usage", totalUsageMsg);
                    this.getMonitor().sendStreamData("system.cpu.user.usage", userUsageMsg);
                    this.getMonitor().sendStreamData("system.cpu.system.usage", systemUsageMsg);
                    this.getMonitor().sendStreamData("system.cpu.usage", cpuUsageMsg);

                    lastMetric.put("system.cpu.total.usage", totalUsageMsg);
                    lastMetric.put("system.cpu.user.usage", userUsageMsg);
                    lastMetric.put("system.cpu.system.usage", systemUsageMsg);
                    lastMetric.put("system.cpu.usage", cpuUsageMsg);

                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void postStart() {

    }

    @Override
    protected void doStart() {
        this.registerTimerTask(new CpuUsageMonitorTimerTask(this.stat, this, this.lastMetric),
                (long) 1000);
    }

    @Override
    protected void preStart() {
        this.stat = (Stat) this.getApiMap().get("linux.stat");
        DataContainer dataContainer = new DataContainer("CpuUsageMonitor");

        Data data = new Data("system_cpu_total_usage",
                this.getDataMethod(),
                "cpu total usage",
                MetricType.GAUGE,
                "system_cpu_total_usage");

        data.addLabel("nodeId", this.getUuid());
        data.addLabel("hostname", this.getHostname());

        dataContainer.registerData(data);

        data = new Data("system_cpu_user_usage",
                this.getDataMethod(),
                "cpu user usage",
                MetricType.GAUGE,
                "system_cpu_user_usage");

        data.addLabel("nodeId", this.getUuid());
        data.addLabel("hostname", this.getHostname());

        dataContainer.registerData(data);

        data = new Data("system_cpu_system_usage",
                this.getDataMethod(),
                "cpu system usage",
                MetricType.GAUGE,
                "system_cpu_system_usage");

        data.addLabel("nodeId", this.getUuid());
        data.addLabel("hostname", this.getHostname());

        dataContainer.registerData(data);

        DataContainerService.registerDataContainer(dataContainer);
    }

    @Override
    public void doStop() {

    }

    @Override
    public Map<String, Map<String, Object>> extract() {
        Map<String, Map<String, Object>> ret = new HashMap<>();
        if(this.lastMetric.containsKey("system.cpu.total.usage"))
            ret.put("system.cpu.total.usage", this.lastMetric.get("system.cpu.total.usage"));
        if(this.lastMetric.containsKey("system.cpu.user.usage"))
            ret.put("system.cpu.user.usage", this.lastMetric.get("system.cpu.user.usage"));
        if(this.lastMetric.containsKey("system.cpu.system.usage"))
            ret.put("system.cpu.system.usage", this.lastMetric.get("system.cpu.system.usage"));
        if(this.lastMetric.containsKey("system.cpu.usage"))
            ret.put("system.cpu.usage", this.lastMetric.get("system.cpu.usage"));
        return ret;
    }

    public GetDataMethod getDataMethod(){
        return (metricName, params) -> {
            switch (metricName) {
                case "system_cpu_total_usage":
                    if(this.lastMetric.containsKey("system.cpu.total.usage"))
                        return (Double) this.lastMetric.get("system.cpu.total.usage").get("value");
                    break;
                case "system_cpu_user_usage":
                    if(this.lastMetric.containsKey("system.cpu.user.usage"))
                        return (Double) this.lastMetric.get("system.cpu.user.usage").get("value");
                    break;
                case "system_cpu_system_usage":
                    if(this.lastMetric.containsKey("system.cpu.system.usage"))
                        return (Double) this.lastMetric.get("system.cpu.system.usage").get("value");
                    break;
            }
            return null;
        };
    }
}
