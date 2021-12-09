package com.github.fenrir.xdashboard.service;

import com.github.fenrir.xmessaging.XMessaging;
import com.github.fenrir.xmessaging.rpc.XMessagingRpcClient;
import org.springframework.stereotype.Service;

@Service
public class XRegistryService {

    private final XMessagingRpcClient rpc;

    public XRegistryService(){
        this.rpc = XMessaging.createRpcClient("XRegistry");
    }
}
