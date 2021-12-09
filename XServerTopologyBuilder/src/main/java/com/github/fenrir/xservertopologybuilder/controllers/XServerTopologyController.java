package com.github.fenrir.xservertopologybuilder.controllers;

import com.github.fenrir.xservertopologybuilder.services.host.HostTopologyService;
import com.github.fenrir.xservertopologybuilder.services.vm.VMTopologyService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path="/server_topology")
public class XServerTopologyController {

    @Getter @Setter
    private HostTopologyService hostTopologyService;

    @Getter @Setter
    private VMTopologyService vmTopologyService;

    public XServerTopologyController(@Autowired HostTopologyService hostTopologyService,
                                     @Autowired VMTopologyService vmTopologyService){
        this.setHostTopologyService(hostTopologyService);
        this.setVmTopologyService(vmTopologyService);
    }

    @RequestMapping(path="/all_hosts", method=RequestMethod.GET)
    public String getAllHosts(){
        return hostTopologyService.getAll();
    }

    @RequestMapping(path = "/all_vms", method = RequestMethod.GET)
    public String getAllVMs(){
        return vmTopologyService.getAll();
    }
}
