package com.github.fenrir.xregistry.rpc;

import com.github.fenrir.xcommon.clients.xregistry.types.rest.BaseResponseMessage;
import com.github.fenrir.xcommon.clients.xregistry.types.rest.LocalMonitorInfoResponseMessage;
import com.github.fenrir.xmessaging.rpc.annotation.RpcServer;
import com.github.fenrir.xregistry.components.ApplicationContextUtil;
import com.github.fenrir.xregistry.services.LocalMonitorService;

@RpcServer(name = "localmonitor")
public class LocalMonitorRpcServer {
    private LocalMonitorService localMonitorService;
    public LocalMonitorRpcServer(){
        this.localMonitorService = ApplicationContextUtil.getBean(LocalMonitorService.class);
    }

    private LocalMonitorService getLocalMonitorService(){
        if(this.localMonitorService == null)
            this.localMonitorService = ApplicationContextUtil.getBean(LocalMonitorService.class);
        return this.localMonitorService;
    }

    public LocalMonitorInfoResponseMessage getAllLocalMonitor(){
        return this.localMonitorService.getAll();
    }

    public BaseResponseMessage deleteLocalMonitorById(String id){
        return this.localMonitorService.deleteById(id);
    }
}
