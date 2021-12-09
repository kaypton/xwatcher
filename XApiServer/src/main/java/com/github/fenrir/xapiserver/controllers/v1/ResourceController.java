package com.github.fenrir.xapiserver.controllers.v1;

import com.github.fenrir.xapiserver.services.resources.v1.XLocalMonitorService;
import com.github.fenrir.xcommon.clients.xapiserver.api.rest.responseEntities.api.v1.XLocalMonitorDeleteResponse;
import com.github.fenrir.xcommon.clients.xapiserver.api.rest.responseEntities.api.v1.XLocalMonitorGetResponse;
import com.github.fenrir.xcommon.clients.xapiserver.api.rest.responseEntities.api.v1.XLocalMonitorUpdateResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/v1/")
public class ResourceController {

    private final XLocalMonitorService xLocalMonitorService;

    public ResourceController(@Autowired XLocalMonitorService xLocalMonitorService){
        this.xLocalMonitorService = xLocalMonitorService;
    }

    @PutMapping(path = "/xlocalmonitor/update/{hostname}/{ipAddress}")
    public XLocalMonitorUpdateResponse updateXLocalMonitor(@PathVariable(name = "hostname") String hostname,
                                              @PathVariable(name = "ipAddress") String ipAddress){
        return xLocalMonitorService.update(hostname, ipAddress);
    }

    @GetMapping(path = "/xlocalmonitor/get/{hostname}/{ipAddress}")
    public XLocalMonitorGetResponse getXLocalMonitor(@PathVariable(name = "hostname") String hostname,
                                                     @PathVariable(name = "ipAddress") String ipAddress){
        return xLocalMonitorService.get(hostname, ipAddress);
    }

    @DeleteMapping(path = "/xlocalmonitor/delete/{hostname}/{ipAddress}")
    public XLocalMonitorDeleteResponse deleteXLocalMonitor(@PathVariable(name = "hostname") String hostname,
                                                           @PathVariable(name = "ipAddress") String ipAddress){
        return xLocalMonitorService.delete(hostname, ipAddress);
    }

    @GetMapping(path = "/xlocalmonitor/getAll")
    public XLocalMonitorGetResponse getAllXLocalMonitor(){
        return xLocalMonitorService.getAll();
    }
}
