package com.github.fenrir.xservertopologybuilder.controllers;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.fenrir.xservertopologybuilder.services.host.HostTopologyService;
import com.github.fenrir.xservertopologybuilder.services.vm.VMTopologyService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(path = "/server_topology/conditional_search/host")
public class HostConditionalSearchController {

    @Getter @Setter private HostTopologyService hostTopologyService = null;

    public HostConditionalSearchController(@Autowired HostTopologyService hostTopologyService){
        this.setHostTopologyService(hostTopologyService);
    }

    @RequestMapping(path = "/cpu_guest_util/higher_than", method = RequestMethod.GET)
    public String searchCpuGuestUtilHigherThan(@RequestParam Double threshold,
                                               @RequestParam Boolean equal){
        List<HostTopologyService.Host> hostList = this.getHostTopologyService()
                .getHostCpuGuestUtilHigherThan(threshold, equal);
        return this.hostList2JSONString(hostList);
    }

    @RequestMapping(path = "/cpu_guest_util/less_than", method = RequestMethod.GET)
    public String searchCpuGuestUtilLessThan(@RequestParam Double threshold,
                                             @RequestParam Boolean equal){
        List<HostTopologyService.Host> hostList = this.getHostTopologyService()
                .getHostCpuGuestUtilLessThan(threshold, equal);
        return this.hostList2JSONString(hostList);
    }

    /**
     * {
     *     "compute1": host json,
     *     "compute2": host json
     * }
     * @param hostList host list
     * @return json string
     */
    private String hostList2JSONString(List<HostTopologyService.Host> hostList){
        JSONObject jsonObject = new JSONObject();
        for(HostTopologyService.Host host : hostList){
            jsonObject.put(host.getHostname(), host);
        }
        return jsonObject.toJSONString();
    }
}
