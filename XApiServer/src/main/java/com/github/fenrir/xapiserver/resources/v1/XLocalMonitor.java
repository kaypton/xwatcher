package com.github.fenrir.xapiserver.resources.v1;

import com.github.fenrir.xcommon.utils.HashUtil;

// /{prefix}/xlocalmonitors/{hostname}-xxx-xxx-xxx-xxx
public class XLocalMonitor {
    static public final String prefix = "xlocalmonitors/";

    private String hostname;
    private String ipAddress;
    private String id;
    private String rpcServerTopic;

    public XLocalMonitor(){}

    public XLocalMonitor(String hostname,
                         String ipAddress){
        this.hostname = hostname;
        this.ipAddress = ipAddress;
        this.id = HashUtil.getSHA256Hash(hostname, ipAddress);
        this.rpcServerTopic = "xlocalmonitor." + this.id;
    }

    public String getHostname(){
        return this.hostname;
    }

    public void setHostname(String hostname){
        this.hostname = hostname;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRpcServerTopic() {
        return rpcServerTopic;
    }

    public void setRpcServerTopic(String rpcServerTopic) {
        this.rpcServerTopic = rpcServerTopic;
    }
}
