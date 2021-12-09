package com.github.fenrir.xlocalmonitor.inspectors.thirdpart.clients.netdataclient.metrics;

public class NetdataCgroupQemuCpuUsagePerCore extends NetdataBaseMetric {
    public int getCoreNum(){
        return this.getLabels().length - 1;
    }
}
