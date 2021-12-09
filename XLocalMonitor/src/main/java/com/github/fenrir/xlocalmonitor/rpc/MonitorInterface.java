package com.github.fenrir.xlocalmonitor.rpc;

import com.github.fenrir.xlocalmonitor.monitors.BaseMonitor;
import com.github.fenrir.xlocalmonitor.services.monitor.XLocalMonitorFactory;
import com.github.fenrir.xmessaging.rpc.annotation.RpcServer;

import java.util.List;
import java.util.Map;

@RpcServer(name = "monitorInterface")
public class MonitorInterface {
    public MonitorInterface(){

    }

    public Map<String, Map<String, Object>> extract(String monitorName){
        BaseMonitor monitor = XLocalMonitorFactory.getMonitorInstanceFromName(monitorName);
        return monitor.extract();
    }

}
