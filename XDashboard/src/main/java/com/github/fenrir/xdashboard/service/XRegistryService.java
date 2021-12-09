package com.github.fenrir.xdashboard.service;

import com.github.fenrir.xcommon.clients.xregistry.api.rest.XRegistryRestAPI;
import com.github.fenrir.xcommon.clients.xregistry.types.rest.BaseResponseMessage;
import com.github.fenrir.xcommon.clients.xregistry.types.rest.LocalMonitorInfoResponseMessage;
import com.github.fenrir.xmessaging.XMessaging;
import com.github.fenrir.xmessaging.rpc.XMessagingRpcClient;
import com.github.fenrir.xmessaging.rpc.exceptions.ErrorStatusException;
import com.github.fenrir.xmessaging.rpc.exceptions.NotRpcReturnMessageException;
import org.springframework.stereotype.Service;

@Service
public class XRegistryService {

    private final XMessagingRpcClient rpc;

    public XRegistryService(){
        this.rpc = XMessaging.createRpcClient("XRegistry");
    }

    public String getAllLocalMonitor(XRegistryRestAPI api){
        return api.getAllLocalMonitor();
    }

    public String deleteLocalMonitor(XRegistryRestAPI api, String id){
        return api.deleteLocalMonitorById(id);
    }

    public LocalMonitorInfoResponseMessage getAllLocalMonitor(){
        try {
            return rpc.call("localmonitor",
                    "getAllLocalMonitor",
                    LocalMonitorInfoResponseMessage.class);
        } catch (ErrorStatusException | NotRpcReturnMessageException e) {
            e.printStackTrace();
            return null;
        }
    }

    public BaseResponseMessage deleteLocalMonitor(String id){
        try {
            return rpc.call("localmonitor",
                    "deleteLocalMonitorById",
                    BaseResponseMessage.class,
                    id);
        } catch (ErrorStatusException | NotRpcReturnMessageException e) {
            e.printStackTrace();
            return null;
        }
    }
}
