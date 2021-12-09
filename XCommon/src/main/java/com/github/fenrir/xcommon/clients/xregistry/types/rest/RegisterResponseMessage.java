package com.github.fenrir.xcommon.clients.xregistry.types.rest;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.fenrir.xcommon.clients.xregistry.types.rest.constants.RegisterStatus;

public class RegisterResponseMessage {

    private RegisterStatus status;
    private String message;
    private String rpcServerTopic;

    public RegisterResponseMessage(){

    }

    public RegisterResponseMessage(String json){
        JSONObject j = JSON.parseObject(json);
        String statusStr = j.getString("status");
        this.message = j.getString("message");
        this.status = RegisterStatus.from(statusStr);
        this.rpcServerTopic = j.getString("rpcServerTopic");
    }

    public String getMessage(){
        return this.message;
    }

    public void setMessage(String message){
        this.message = message;
    }

    public String toJSONString(){
        return JSON.toJSONString(this);
    }

    public RegisterStatus getStatus() {
        return status;
    }

    public void setStatus(RegisterStatus status) {
        this.status = status;
    }

    public String getRpcServerTopic() {
        return rpcServerTopic;
    }

    public void setRpcServerTopic(String rpcServerTopic) {
        this.rpcServerTopic = rpcServerTopic;
    }
}
