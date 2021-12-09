package com.github.fenrir.xopenstackagent.controllers;

import com.alibaba.fastjson.JSONObject;
import com.github.fenrir.xopenstackagent.services.OpenStackVMOperationService;
import lombok.Getter;
import lombok.Setter;
import org.openstack4j.model.common.ActionResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/server")
public class VMOperationController {

    @Getter @Setter private OpenStackVMOperationService service;

    public VMOperationController(@Autowired OpenStackVMOperationService service){
        this.setService(service);
    }

    @RequestMapping(path = "/liveMigrate", method = RequestMethod.GET)
    public ActionResponse serverLiveMigrate(@RequestParam Boolean blockMigration,
                                    @RequestParam Boolean diskOverCommit,
                                    @RequestParam String host,
                                    @RequestParam String serverId){

        return this.getService().vmLiveMigrate(blockMigration, diskOverCommit, host, serverId);
    }
}
