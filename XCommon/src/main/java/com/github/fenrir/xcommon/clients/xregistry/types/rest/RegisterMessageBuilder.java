package com.github.fenrir.xcommon.clients.xregistry.types.rest;

import com.github.fenrir.xcommon.clients.xlocalmonitor.types.LocalMonitorType;

public class RegisterMessageBuilder {

    private RegisterMessage registerMessage;

    private RegisterMessage getRegisterMessage(){
        if(this.registerMessage == null)
            this.registerMessage = new RegisterMessage();
        return this.registerMessage;
    }

    private RegisterMessageBuilder(){
    }

    public RegisterMessage build(){
        return this.registerMessage;
    }

    public RegisterMessageBuilder setHostname(String hostname){
        this.getRegisterMessage().setHostname(hostname);
        return this;
    }

    public RegisterMessageBuilder setIpAddr(String ipAddr){
        this.getRegisterMessage().setIpAddr(ipAddr);
        return this;
    }

    public RegisterMessageBuilder setLocalMonitorType(LocalMonitorType localMonitorType){
        this.getRegisterMessage().setLocalMonitorType(localMonitorType);
        return this;
    }

    public static RegisterMessage fromJSONString(String json){
        return new RegisterMessage(json);
    }

    static public RegisterMessageBuilder builder(){
        return new RegisterMessageBuilder();
    }
}
