package com.github.fenrir.xlocalmonitor.monitors.oshi;

import com.github.fenrir.xcommon.utils.CommonUtils;
import com.github.fenrir.xlocalmonitor.annotations.Monitor;
import com.github.fenrir.xlocalmonitor.monitors.BaseMonitor;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;

import java.util.List;
import java.util.Map;
import java.util.TimerTask;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Monitor(name = "HostInfoMonitor",
        streams = {"host.info"})
public class HostInfoMonitor extends BaseMonitor {
    private static final Logger logger = LoggerFactory.getLogger(
            "HostInfoMonitor");

    public static String getMonitorName(){
        return "HostInfoMonitor";
    }

    @Getter @Setter private Lock lock = new ReentrantLock();
    @Getter @Setter private Condition stop = lock.newCondition();

    private static class JavaHostInfoMonitorTimerTask extends TimerTask {
        @Getter @Setter private HostInfoMonitor monitor;

        public JavaHostInfoMonitorTimerTask(HostInfoMonitor monitor){
            this.setMonitor(monitor);
        }

        @Override
        public void run(){
            SystemInfo systemInfo = new SystemInfo();
            CentralProcessor processor = systemInfo.getHardware().getProcessor();
            GlobalMemory memory = systemInfo.getHardware().getMemory();

            // cpu 逻辑核数
            int cpuCoreNum = processor.getLogicalProcessorCount();

            // 内存总量
            long totalByte = memory.getTotal();
            long totalKiB = totalByte / 1024;

            this.getMonitor().pushStreamData(
                    "host.info",
                    this.getMonitor().createStreamData(
                            "host.info",
                            this.getMonitor().getHostname(),
                            cpuCoreNum,
                            totalKiB,
                            CommonUtils.getTimestamp()
                    )
            );
        }
    }

    @Override
    public Map<String, Map<String, Object>> extract(){
        return null;
    }

    public HostInfoMonitor() {}

    @Override
    protected void postStart() {
        logger.info("monitor stop running ... ");
    }

    @Override
    protected void doStart() {
        this.registerTimerTask(new JavaHostInfoMonitorTimerTask(this), (long) 10000);

        lock.lock();
        try {
            stop.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void preStart(){
        // registerStream("host.info");
        logger.info("monitor start running ...");
    }

    @Override
    public void doStop() {
        this.stop.notify();
    }
}
