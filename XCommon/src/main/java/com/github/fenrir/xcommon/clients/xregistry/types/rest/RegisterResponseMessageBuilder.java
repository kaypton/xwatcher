package com.github.fenrir.xcommon.clients.xregistry.types.rest;

import com.github.fenrir.xcommon.clients.xregistry.types.rest.constants.RegisterStatus;

public class RegisterResponseMessageBuilder {

    private RegisterResponseMessage registerResponseMessage = null;

    private RegisterResponseMessage getRegisterResponseMessage(){
        if(this.registerResponseMessage == null)
            this.registerResponseMessage = new RegisterResponseMessage();
        return this.registerResponseMessage;
    }

    private RegisterResponseMessageBuilder(){

    }

    public RegisterResponseMessage build(){
        return this.registerResponseMessage;
    }

    public RegisterResponseMessageBuilder setStatus(RegisterStatus status){
        this.getRegisterResponseMessage().setStatus(status);
        return this;
    }

    public RegisterResponseMessageBuilder setMessage(String message){
        this.getRegisterResponseMessage().setMessage(message);
        return this;
    }

    public RegisterResponseMessageBuilder setRpcServerTopic(String rpcServerTopic){
        this.getRegisterResponseMessage().setRpcServerTopic(rpcServerTopic);
        return this;
    }

    public RegisterResponseMessage fromJSONString(String json){
        return new RegisterResponseMessage(json);
    }

    static public RegisterResponseMessageBuilder builder(){
        return new RegisterResponseMessageBuilder();
    }
}
