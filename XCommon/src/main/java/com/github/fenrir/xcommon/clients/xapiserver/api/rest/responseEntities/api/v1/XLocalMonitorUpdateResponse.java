package com.github.fenrir.xcommon.clients.xapiserver.api.rest.responseEntities.api.v1;

import com.github.fenrir.xcommon.clients.xapiserver.api.rest.responseEntities.BaseResponse;

public class XLocalMonitorUpdateResponse extends BaseResponse {
    private String rpcServerTopic;

    public String getRpcServerTopic() {
        return rpcServerTopic;
    }

    public void setRpcServerTopic(String rpcServerTopic) {
        this.rpcServerTopic = rpcServerTopic;
    }
}
