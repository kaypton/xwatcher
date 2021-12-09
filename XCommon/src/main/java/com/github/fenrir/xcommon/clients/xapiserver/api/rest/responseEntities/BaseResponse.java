package com.github.fenrir.xcommon.clients.xapiserver.api.rest.responseEntities;

public class BaseResponse {
    protected int status;
    protected String msg;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
