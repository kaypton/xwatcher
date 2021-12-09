package com.github.fenrir.xlocalmonitor.monitors.netdata;

import com.github.fenrir.xcommon.utils.CommonUtils;
import com.github.fenrir.xlocalmonitor.annotations.Monitor;
import com.github.fenrir.xlocalmonitor.inspectors.thirdpart.clients.netdataclient.NetdataAPI;
import com.github.fenrir.xlocalmonitor.inspectors.thirdpart.clients.netdataclient.metrics.NetdataPerCoreUtilMetric;
import com.github.fenrir.xlocalmonitor.monitors.BaseMonitor;
import lombok.Getter;
import lombok.Setter;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;

@Monitor(name = "NetdataPerCoreUtilMonitor",
        streams = {"system.per.core.user.util"},
        inspectors = {"netdata"})
public class NetdataPerCoreUtilMonitor extends BaseMonitor {

    private final int coreNum;

    private static class Task extends TimerTask {

        @Getter @Setter private NetdataPerCoreUtilMonitor monitor;
        @Getter @Setter private NetdataAPI api;
        @Getter @Setter private int coreNum;

        public Task(NetdataPerCoreUtilMonitor monitor,
                    NetdataAPI api,
                    int coreNum){
            this.setMonitor(monitor);
            this.setApi(api);
            this.setCoreNum(coreNum);
        }

        @Override
        public void run() {
            List<Double> values = new ArrayList<>();
            List<Long> timestamps = new ArrayList<>();

            NetdataPerCoreUtilMetric value;
            for(int i = 0; i < this.coreNum; i++){
                value = api.getPerCoreUtil(i);
                values.add(value.getLatestMetric(NetdataPerCoreUtilMetric.Kind.USER, Double.class).getValue());
                timestamps.add(CommonUtils.getTimestamp());
            }

            this.getMonitor().pushStreamData(
                    "system.per.core.user.util",
                    this.getMonitor().createStreamData(
                            "system.per.core.user.util",
                            this.getMonitor().getHostname(),
                            values,
                            timestamps
                    )
            );
        }
    }

    public NetdataPerCoreUtilMonitor(){
        SystemInfo systemInfo = new SystemInfo();
        CentralProcessor processor = systemInfo.getHardware().getProcessor();
        this.coreNum = processor.getLogicalProcessorCount();
    }

    @Override
    protected void postStart() {

    }

    @Override
    protected void doStart() {
        this.registerTimerTask(
                new Task(this,
                        (NetdataAPI) this.getApiMap().get("netdata"),
                        coreNum), (long) 1000
        );
    }

    @Override
    protected void preStart() {

    }

    @Override
    public void doStop() {

    }

    @Override
    public Map<String, Map<String, Object>> extract() {
        return null;
    }
}
