package com.github.fenrir.xlocalmonitor.monitors.netdata;

import com.github.fenrir.xlocalmonitor.annotations.Monitor;
import com.github.fenrir.xlocalmonitor.inspectors.thirdpart.clients.netdataclient.NetdataAPI;
import com.github.fenrir.xlocalmonitor.inspectors.thirdpart.clients.netdataclient.metrics.NetdataMemUtilMetric;
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

@Monitor(name = "NetdataRamFreeMonitor", streams = {"system.ram.free"},
         inspectors = {"netdata"})
public class NetdataRamFreeMonitor extends BaseMonitor {
    private static final Logger logger = LoggerFactory.getLogger(
            "NetdataRamFreeMonitor");

    public static String getMonitorName(){
        return "NetdataRamFreeMonitor";
    }

    @Getter @Setter private Lock lock = new ReentrantLock();
    @Getter @Setter private Condition stop = lock.newCondition();

    private static class NetdataRamFreeMonitorTimerTask extends TimerTask {

        @Getter @Setter private NetdataRamFreeMonitor monitor;
        @Getter @Setter private NetdataAPI api;

        public NetdataRamFreeMonitorTimerTask(NetdataRamFreeMonitor monitor,
                                              NetdataAPI api){
            this.setMonitor(monitor);
            this.setApi(api);
        }

        @Override
        public void run() {
            NetdataMemUtilMetric metric = this.getApi().getMemUtil();
            Value<Double> value = metric.getLatestMetric(NetdataMemUtilMetric.Kind.FREE, Double.class);
            this.getMonitor().pushStreamData(
                    "system.ram.free",
                    this.getMonitor().createStreamData(
                            "system.ram.free",
                            this.getMonitor().getHostname(), value.getValue(), value.getTimestamp()
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
        this.registerTimerTask(new NetdataRamFreeMonitorTimerTask(
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
        // registerStream("system.ram.free");
        logger.info("monitor start running ... ");
    }

    @Override
    public void doStop() {
        this.stop.notify();
    }
}
