package com.github.fenrir.xservertopologybuilder.controllers;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.fenrir.xservertopologybuilder.services.vm.VMTopologyService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(path = "/server_topology/conditional_search/vm")
public class VMConditionalSearchController {

    @Getter @Setter private VMTopologyService vmTopologyService = null;

    public VMConditionalSearchController(@Autowired VMTopologyService vmTopologyService){
        this.setVmTopologyService(vmTopologyService);
    }

    /**
     * 请求体：<br/>
     * {
     *     "hosts":[
     *          "compute1",
     *          "compute2"
     *     ]
     * }
     * @param payload 请求体
     * @return json
     */
    @RequestMapping(path = "/get_vm_from_host",
            method = RequestMethod.POST,
            produces = "application/json; charset=UTF-8")
    public String getVMByHostname(@RequestBody JSONObject payload){
        JSONArray hostArray = payload.getJSONArray("hosts");
        List<String> hostnames = new ArrayList<>();
        for(int i = 0; i < hostArray.size(); i++){
            hostnames.add(hostArray.getString(i));
        }
        List<VMTopologyService.VM> vmList =
                this.getVmTopologyService().getVMByHostname(hostnames);

        JSONArray vmArray = new JSONArray();
        vmArray.addAll(vmList);

        JSONObject ret = new JSONObject();
        ret.put("vms", vmArray);
        return ret.toJSONString();
    }
}
