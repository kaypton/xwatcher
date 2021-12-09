package com.github.fenrir.xdashboard.controller;

import com.github.fenrir.xcommon.clients.xlocalmonitor.entities.LocalMonitorOverview;
import com.github.fenrir.xdashboard.service.XLocalMonitorService;
import com.github.fenrir.xdashboard.service.XRegistryService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping(path = "/api/v1/localmonitor")
public class LocalMonitorController {

    private final XRegistryService xRegistryService;
    private final XLocalMonitorService xLocalMonitorService;

    public LocalMonitorController(@Value("${dashboard.xregistryAddress}") String xregistryAddress,
                                  @Autowired XRegistryService xRegistryService,
                                  @Autowired XLocalMonitorService xLocalMonitorService){
        this.xRegistryService = xRegistryService;
        this.xLocalMonitorService = xLocalMonitorService;
    }

    @ApiOperation("get overview by rpc server topic")
    @RequestMapping(
            path = "/overview/{rpcServerTopic}",
            method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    public LocalMonitorOverview getOverviewById(@PathVariable String rpcServerTopic){
        return this.xLocalMonitorService.getOverview(rpcServerTopic);
    }

    @RequestMapping(
            path = "/extract/{rpcServerTopic}/{monitorName}",
            method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    public Map<String, Map<String, Object>> extract(@PathVariable(name = "rpcServerTopic") String rpcServerTopic,
                                             @PathVariable(name = "monitorName") String monitorName){
        return this.xLocalMonitorService.extract(rpcServerTopic, monitorName);
    }
}
