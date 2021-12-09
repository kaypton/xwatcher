package com.github.fenrir.xopenstackagent.controllers;

import com.github.fenrir.xcommon.clients.xopenstackagent.api.rest.entities.ListVirtualServerResponse;
import com.github.fenrir.xopenstackagent.services.OpenStackListingService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/list")
public class ListController {

    @Getter @Setter private OpenStackListingService service;

    public ListController(@Autowired OpenStackListingService service){
        this.setService(service);
    }

    @RequestMapping(path = "/virtual_servers", method = RequestMethod.GET)
    public ListVirtualServerResponse listVirtualServers(){
        return this.getService().listVirtualServers();
    }
}
