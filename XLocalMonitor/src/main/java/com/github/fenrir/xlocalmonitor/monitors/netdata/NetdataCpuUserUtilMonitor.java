package com.github.fenrir.xlocalmonitor.monitors.netdata;

import com.github.fenrir.xlocalmonitor.annotations.Monitor;
import com.github.fenrir.xlocalmonitor.inspectors.thirdpart.clients.netdataclient.NetdataAPI;
import com.github.fenrir.xlocalmonitor.inspectors.thirdpart.clients.netdataclient.metrics.NetdataCpuUtilMetric;
import com.github.fenrir.xlocalmonitor.inspectors.thirdpart.clients.netdataclient.metrics.Value;
import com.github.fenrir.xlocalmonitor.monitors.BaseMonitor;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.TimerTask;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Monitor(name = "NetdataCpuUserUtilMonitor",
         streams = {"system.cpu.user.util"},
         inspectors = {"netdata"})
public class NetdataCpuUserUtilMonitor extends BaseMonitor {
    private static final Logger logger = LoggerFactory.getLogger(
            "NetdataCpuUserUtilMonitor");

    public static String getMonitorName(){
        return "NetdataCpuUserUtilMonitor";
    }

    @Getter @Setter private Lock lock = new ReentrantLock();
    @Getter @Setter private Condition stop = lock.newCondition();

    private static class NetdataCpuUserUtilMonitorTimerTask extends TimerTask {

        @Getter @Setter NetdataCpuUserUtilMonitor monitor;
        @Getter @Setter NetdataAPI api;

        public NetdataCpuUserUtilMonitorTimerTask(NetdataCpuUserUtilMonitor monitor,
                                                  NetdataAPI api){
            this.setMonitor(monitor);
            this.setApi(api);
        }

        @Override
        public void run() {
            NetdataCpuUtilMetric metric = api.getCpuUtil();
            Value<Double> value = metric.getLatestMetric(NetdataCpuUtilMetric.Kind.USER, Double.class);
            this.getMonitor().pushStreamData(
                    "system.cpu.user.util",
                    this.getMonitor().createStreamData(
                            "system.cpu.user.util",
                            this.getMonitor().getHostname(), value.getValue(), value.getTimestamp()
                    )
            );
        }
    }

    @Override
    protected void postStart() {
        logger.info("monitor stop running ... ");
    }

    @Override
    protected void preStart() {
        // registerStream("system.cpu.user.util");
        logger.info("monitor start running ... ");
    }

    @Override
    public Map<String, Map<String, Object>> extract(){
        return null;
    }

    @Override
    protected void doStart() {
        this.registerTimerTask(new NetdataCpuUserUtilMonitorTimerTask(
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
    public void doStop() {
        this.stop.notify();
    }
}
