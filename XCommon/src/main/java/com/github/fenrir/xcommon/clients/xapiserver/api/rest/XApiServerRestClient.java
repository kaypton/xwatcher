package com.github.fenrir.xcommon.clients.xapiserver.api.rest;

import com.github.fenrir.xcommon.clients.xapiserver.api.rest.resources.v1.XLocalMonitorClient;

public class XApiServerRestClient {

    private String host;
    private final boolean enableTLS;
    private String schema;

    private final XLocalMonitorClient xLocalMonitorClient;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public boolean isEnableTLS() {
        return enableTLS;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public XLocalMonitorClient getXLocalMonitorClient() {
        return xLocalMonitorClient;
    }

    public static class XApiServerRestClientBuilder {

        private String host;
        private boolean enableTLS = false;

        public XApiServerRestClientBuilder host(String host){
            this.host = host;
            return this;
        }

        public XApiServerRestClientBuilder enableTLS(){
            this.enableTLS = true;
            return this;
        }

        public XApiServerRestClient build(){
            return new XApiServerRestClient(this.host, this.enableTLS);
        }
    }

    static public XApiServerRestClientBuilder builder(){
        return new XApiServerRestClientBuilder();
    }

    public XApiServerRestClient(String host,
                                boolean enableTLS){
        this.host = host;
        this.enableTLS = enableTLS;
        if(enableTLS){
            this.schema = "https://";
        }else this.schema = "http://";

        this.xLocalMonitorClient = new XLocalMonitorClient(this.schema + this.host);
    }
}
