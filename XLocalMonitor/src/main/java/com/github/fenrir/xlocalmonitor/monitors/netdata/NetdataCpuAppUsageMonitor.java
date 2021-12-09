package com.github.fenrir.xlocalmonitor.monitors.netdata;

import com.github.fenrir.xlocalmonitor.annotations.Monitor;
import com.github.fenrir.xlocalmonitor.inspectors.thirdpart.clients.netdataclient.NetdataAPI;
import com.github.fenrir.xlocalmonitor.inspectors.thirdpart.clients.netdataclient.metrics.NetdataCpuAppUsageMetric;
import com.github.fenrir.xlocalmonitor.inspectors.thirdpart.clients.netdataclient.metrics.Value;
import com.github.fenrir.xlocalmonitor.monitors.BaseMonitor;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Monitor(name = "NetdataCpuAppUsageMonitor", streams = {"system.cpu.app.usage"},
         inspectors = {"netdata"})
public class NetdataCpuAppUsageMonitor extends BaseMonitor {
    private static final Logger logger = LoggerFactory.getLogger(
            "NetdataCpuAppUsageMonitor");

    public static String getMonitorName(){
        return "NetdataCpuAppUsageMonitor";
    }

    @Getter @Setter private Lock lock = new ReentrantLock();
    @Getter @Setter private Condition stop = lock.newCondition();

    private static class NetdataCpuAppUsageMonitorTimerTask extends TimerTask {

        @Getter @Setter private NetdataCpuAppUsageMonitor monitor;
        @Getter @Setter private NetdataAPI api;

        public NetdataCpuAppUsageMonitorTimerTask(NetdataCpuAppUsageMonitor monitor,
                                                   NetdataAPI api){
            this.setMonitor(monitor);
            this.setApi(api);
        }
        @Override
        public void run() {
            NetdataCpuAppUsageMetric metric = this.getApi().getCpuAppUsageMetric();
            if(metric == null) return;

            List<String> apps = metric.getAppList();

            List<String> availableApps = new ArrayList<>();
            List<Double> values = new ArrayList<>();

            Long timestamp = null;

            for(String appName : apps){
                Value<Double> usage = metric.getLatestMetricByName(appName, Double.class);
                if(usage != null){
                    availableApps.add(appName);
                    values.add(usage.getValue());
                }
                if(timestamp == null && usage != null)
                    timestamp = usage.getTimestamp();
            }

            this.getMonitor().pushStreamData(
                    "system.cpu.app.usage",
                    this.getMonitor().createStreamData(
                            "system.cpu.app.usage",
                            this.getMonitor().getHostname(),
                            availableApps,
                            values,
                            timestamp
                    )
            );
        }
    }

    @Override
    public Map<String, Map<String, Object>> extract(){
        return null;
    }

    @Override
    protected void postStart() {
        logger.info("monitor stop running ... ");
    }

    @Override
    protected void doStart() {
        this.registerTimerTask(new NetdataCpuAppUsageMonitorTimerTask(
                this, (NetdataAPI) this.getApiMap().get("netdata")
        ), (long) 1000);

        lock.lock();
        try {
            stop.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void preStart() {
        // registerStream("system.cpu.app.usage");
        logger.info("monitor start running ... ");
    }

    @Override
    public void doStop() {
        this.stop.notify();
    }
}
