package com.github.fenrir.xcommon.clients.xregistry.entities.mongo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.data.annotation.Id;

public class LocalMonitor {
    @Id
    private String localMonitorId;
    private String hostname;
    private String ip;
    private String rpcServerTopic;
    private String localMonitorType;

    public String getLocalMonitorId() {
        return localMonitorId;
    }

    public void setLocalMonitorId(String localMonitorId) {
        this.localMonitorId = localMonitorId;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ipAddr) {
        this.ip = ipAddr;
    }

    public String getRpcServerTopic() {
        return rpcServerTopic;
    }

    public void setRpcServerTopic(String rpcServerTopic) {
        this.rpcServerTopic = rpcServerTopic;
    }

    public String getLocalMonitorType() {
        return localMonitorType;
    }

    public void setLocalMonitorType(String localMonitorType) {
        this.localMonitorType = localMonitorType;
    }

    public String toJSONString(){
        return JSON.toJSONString(this);
    }
}
