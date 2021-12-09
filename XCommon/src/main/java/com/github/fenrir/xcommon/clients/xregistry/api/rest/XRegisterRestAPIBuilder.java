package com.github.fenrir.xcommon.clients.xregistry.api.rest;

import org.springframework.web.client.RestTemplate;

public class XRegisterRestAPIBuilder {

    private String host;

    private XRegisterRestAPIBuilder(){

    }

    public XRegistryRestAPI build(){
        return new XRegistryRestAPI(new RestTemplate(), host);
    }

    public XRegisterRestAPIBuilder setHost(String host){
        this.host = host;
        return this;
    }

    public static XRegisterRestAPIBuilder builder(){
        return new XRegisterRestAPIBuilder();
    }
}
