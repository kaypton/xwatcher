package com.github.fenrir.xcommon.clients.xregistry.types.rest;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.fenrir.xcommon.clients.xlocalmonitor.types.LocalMonitorType;

public class RegisterMessage {

    private String hostname;
    private LocalMonitorType localMonitorType;
    private String ipAddr;

    public RegisterMessage(){

    }

    public RegisterMessage(String json){
        JSONObject j = JSON.parseObject(json);
        this.hostname = j.getString("hostname");
        this.ipAddr = j.getString("ipAddr");
        this.localMonitorType = LocalMonitorType.from(j.getString("localMonitorType"));
    }

    public String toJSONString(){
        return JSON.toJSONString(this);
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public LocalMonitorType getLocalMonitorType() {
        return localMonitorType;
    }

    public void setLocalMonitorType(LocalMonitorType localMonitorType) {
        this.localMonitorType = localMonitorType;
    }

    public String getIpAddr() {
        return ipAddr;
    }

    public void setIpAddr(String ipAddr) {
        this.ipAddr = ipAddr;
    }
}
