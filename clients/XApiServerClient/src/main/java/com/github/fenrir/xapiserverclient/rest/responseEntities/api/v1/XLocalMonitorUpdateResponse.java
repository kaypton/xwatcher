package com.github.fenrir.xapiserverclient.rest.responseEntities.api.v1;

import com.github.fenrir.xapiserverclient.rest.responseEntities.BaseResponse;

public class XLocalMonitorUpdateResponse extends BaseResponse {
    private String rpcServerTopic;

    public String getRpcServerTopic() {
        return rpcServerTopic;
    }

    public void setRpcServerTopic(String rpcServerTopic) {
        this.rpcServerTopic = rpcServerTopic;
    }
}
