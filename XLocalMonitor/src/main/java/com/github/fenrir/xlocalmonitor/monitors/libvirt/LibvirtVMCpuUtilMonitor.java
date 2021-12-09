package com.github.fenrir.xlocalmonitor.monitors.libvirt;

import com.github.fenrir.xlocalmonitor.annotations.Monitor;
import com.github.fenrir.xlocalmonitor.inspectors.thirdpart.clients.libvirtclient.LibvirtAPI;
import com.github.fenrir.xlocalmonitor.monitors.BaseMonitor;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Monitor(name = "LibvirtVMCpuUtilMonitor",
        streams = {"libvirt.vm.cpu.util"},
        inspectors = {"libvirt"})
public class LibvirtVMCpuUtilMonitor extends BaseMonitor {
    private static final Logger logger = LoggerFactory.getLogger(
            "LibvirtVMCpuUtilMonitor");

    @Getter @Setter private Lock lock = new ReentrantLock();
    @Getter @Setter private Condition stop = lock.newCondition();

    private static class VMCpuTimerTask extends TimerTask {

        private static class CpuTimeMetric {
            /**
             * when we got the metric
             */
            @Getter @Setter private Long timestamp;
            @Getter @Setter private Long cpuTime;

            public CpuTimeMetric(Long timestamp, Long cpuTime){
                this.setCpuTime(cpuTime);
                this.setTimestamp(timestamp);
            }
        }

        @Getter @Setter private LibvirtAPI libvirtAPI;
        @Getter @Setter private LibvirtVMCpuUtilMonitor monitor;
        @Getter @Setter private String uuid;

        @Getter @Setter private Map<Integer, CpuTimeMetric> metricMap =
                new HashMap<>();

        public VMCpuTimerTask(LibvirtAPI api,
                              LibvirtVMCpuUtilMonitor monitor,
                              String uuid){
            api.connect();
            this.setMonitor(monitor);
            this.setLibvirtAPI(api);
            this.setUuid(uuid);
        }

        @Override
        public void run() {
            int[] vmids = this.getLibvirtAPI().getAllActiveDomainIds();
            if(vmids != null){
                for(int vmid : vmids){
                    CpuTimeMetric metric = this.getMetricMap().get(vmid);

                    long timestamp = System.currentTimeMillis();
                    long cpuTime = this.getLibvirtAPI().getCpuTimeByDomainId(vmid);

                    if(cpuTime == -1){
                        this.getMetricMap().remove(vmid);
                    }else{
                        if(metric == null){
                            this.getMetricMap().put(vmid, new CpuTimeMetric(timestamp, cpuTime));
                        }else{
                            long _cpuTime = cpuTime - metric.getCpuTime();
                            long _duration = timestamp - metric.getTimestamp();

                            Double util = ((double) _cpuTime / 1000) / (double) _duration;

                            // send metric
                            this.getMonitor().pushStreamData(
                                    "libvirt.vm.cpu.util",
                                    this.getMonitor().createStreamData(
                                            "libvirt.vm.cpu.util",
                                            this.getMonitor().getHostname(),
                                            vmid,
                                            util,
                                            timestamp));
                        }
                    }
                }
            }
        }
    }

    @Override
    public Map<String, Map<String, Object>> extract(){
        return null;
    }

    @Override
    protected void postStart() {

    }

    @Override
    protected void doStart() {
        this.registerTimerTask(new VMCpuTimerTask(
                (LibvirtAPI) this.getApiMap().get("libvirt"),
                this,
                this.getUuid()
        ), (long) 1000);
        try{
            stop.await();
        } catch (InterruptedException e){
            e.printStackTrace();
        }
    }

    @Override
    protected void preStart() {
        logger.info("monitor start running ...");
        // registerEvent("libvirt.vm.cpu.util");
    }

    @Override
    public void doStop() {
        this.stop.notify();
    }
}
