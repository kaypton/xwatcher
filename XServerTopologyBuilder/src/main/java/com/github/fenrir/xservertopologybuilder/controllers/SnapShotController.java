package com.github.fenrir.xservertopologybuilder.controllers;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.fenrir.xservertopologybuilder.services.SnapShotService;
import com.github.fenrir.xservertopologybuilder.services.host.HostTopologyService;
import com.github.fenrir.xservertopologybuilder.services.vm.VMTopologyService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping(path = "/server_topology/snapshot")
public class SnapShotController {

    @Getter @Setter private SnapShotService service;

    public SnapShotController(@Autowired SnapShotService service){
        this.setService(service);
    }

    @RequestMapping(path = "/entire", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public String getEntireSnapShot(){
        Map<HostTopologyService.Host, Set<VMTopologyService.VM>> snapShot =
                this.getService().getEntireSnapShot();
        JSONObject ret = new JSONObject();
        for(HostTopologyService.Host host : snapShot.keySet()){
            JSONObject hostJSON = new JSONObject();
            JSONArray vmArray = new JSONArray();
            vmArray.addAll(snapShot.get(host));
            hostJSON.put("info", host);
            hostJSON.put("vms", vmArray);
            ret.put(host.getHostname(), hostJSON);
        }
        return ret.toJSONString();
    }
}
