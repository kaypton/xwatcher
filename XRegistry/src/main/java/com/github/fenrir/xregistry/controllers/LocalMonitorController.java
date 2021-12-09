package com.github.fenrir.xregistry.controllers;

import com.github.fenrir.xcommon.clients.xregistry.types.rest.BaseResponseMessage;
import com.github.fenrir.xcommon.clients.xregistry.types.rest.RegisterMessage;
import com.github.fenrir.xcommon.clients.xregistry.types.rest.RegisterMessageBuilder;
import com.github.fenrir.xcommon.clients.xregistry.types.rest.RegisterResponseMessage;
import com.github.fenrir.xcommon.clients.xregistry.types.rest.LocalMonitorInfoResponseMessage;
import com.github.fenrir.xregistry.services.LocalMonitorService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/v1/localmonitor")
public class LocalMonitorController {

    private final LocalMonitorService localMonitorService;

    public LocalMonitorController(@Autowired LocalMonitorService localMonitorService){
        this.localMonitorService = localMonitorService;
    }

    @ApiOperation(
            value = "Register a localmonitor")
    @RequestMapping(
            path = "/register",
            method = RequestMethod.POST,
            produces = "application/json; charset=utf-8",
            consumes = "application/json; charset=utf-8")
    public RegisterResponseMessage register(@RequestBody RegisterMessage body){
        return this.localMonitorService.register(body);
    }

    @ApiOperation(
            value="get all localmonitor's information")
    @RequestMapping(
            path = "/info/all",
            method = RequestMethod.GET)
    public LocalMonitorInfoResponseMessage getAllLocalMonitors(){
        return this.localMonitorService.getAll();
    }

    @ApiOperation(
            value="delete a localmonitor from registry repository")
    @ApiImplicitParam(
            name = "id",
            dataType = "String",
            value = "localmonitor id",
            required = true,
            example = "localmonitor.1b00c4531806fbfa4d8d2a112d51e286f44ffdfb8c83f7a003392c1ca89f4d5d")
    @RequestMapping(
            path = "/delete/{id}",
            method = RequestMethod.GET)
    public BaseResponseMessage deleteLocalMonitor(@PathVariable(name = "id") String id){
        return this.localMonitorService.deleteById(id);
    }
}
