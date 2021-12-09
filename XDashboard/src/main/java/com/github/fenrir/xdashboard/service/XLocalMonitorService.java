package com.github.fenrir.xdashboard.service;

import com.alibaba.fastjson.JSON;
import com.github.fenrir.xcommon.clients.xlocalmonitor.entities.LocalMonitorOverview;
import com.github.fenrir.xmessaging.XMessaging;
import com.github.fenrir.xmessaging.rpc.XMessagingRpcClient;
import com.github.fenrir.xmessaging.rpc.exceptions.ErrorStatusException;
import com.github.fenrir.xmessaging.rpc.exceptions.NotRpcReturnMessageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;

@Service
public class XLocalMonitorService {
    static private final Logger logger = LoggerFactory.getLogger("XLocalMonitorService");

    public XLocalMonitorService(){

    }

    private XMessagingRpcClient getRpcClient(String serverName){
        XMessagingRpcClient client = XMessaging.createRpcClient(serverName);
        logger.info("create client for server " + serverName);
        return client;
    }

    public LocalMonitorOverview getOverview(String serverName){
        logger.info("get overview of " + serverName);
        XMessagingRpcClient rpc = this.getRpcClient(serverName);
        try {
            LocalMonitorOverview overview = rpc.call("genericInfo",
                    "getOverview",
                    LocalMonitorOverview.class);
            logger.info(JSON.toJSONString(overview));
            return overview;
        } catch (ErrorStatusException | NotRpcReturnMessageException | CancellationException e) {
            e.printStackTrace();
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Map<String, Object>> extract(String serverName, String monitorName){
        logger.info("extract from {}", serverName);
        XMessagingRpcClient rpc = this.getRpcClient(serverName);
        try{
            return rpc.call("monitorInterface",
                    "extract",
                    Map.class,
                    monitorName);
        } catch (ErrorStatusException | NotRpcReturnMessageException | CancellationException e) {
            e.printStackTrace();
            return null;
        }
    }
}
