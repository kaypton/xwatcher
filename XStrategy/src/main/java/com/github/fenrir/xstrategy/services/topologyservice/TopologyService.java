package com.github.fenrir.xstrategy.services.topologyservice;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.fenrir.xstrategy.restapis.ServerTopologyRestAPI;
import com.github.fenrir.xstrategy.restapis.XPlannerRestAPI;
import com.github.fenrir.xstrategy.services.topologyservice.resource.Host;
import com.github.fenrir.xstrategy.services.topologyservice.resource.SnapShotEnvironment;
import com.github.fenrir.xstrategy.services.topologyservice.resource.VirtualMachine;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class TopologyService {

    @Getter @Setter private XPlannerRestAPI xPlannerRestAPI;
    @Getter @Setter private ServerTopologyRestAPI serverTopologyRestAPI;

    public TopologyService(@Autowired XPlannerRestAPI xPlannerRestAPI,
                           @Autowired ServerTopologyRestAPI serverTopologyRestAPI){
        this.setXPlannerRestAPI(xPlannerRestAPI);
        this.setServerTopologyRestAPI(serverTopologyRestAPI);
    }

    public SnapShotEnvironment getEntireSnapShotEnvironment(){
        JSONObject serverTopologyJSON = JSON.parseObject(this.getServerTopologyRestAPI()
                .getEntireSnapShot());
        Set<String> hostnameSet = serverTopologyJSON.keySet();
        SnapShotEnvironment snapShotEnvironment = SnapShotEnvironment.build();
        for(String hostname : hostnameSet){
            JSONObject hostJSON = serverTopologyJSON.getJSONObject(hostname);
            Host host = new Host(
                    hostJSON.getJSONObject("info").getString("hostname"),
                    hostJSON.getJSONObject("info").getDouble("idle_cpu_usage"),
                    hostJSON.getJSONObject("info").getDouble("vm_cpu_usage"),
                    hostJSON.getJSONObject("info").getInteger("cpu_core_num"),
                    hostJSON.getJSONObject("info").getLong("memory_total_kb")
            );
            JSONArray vmJSONArray = hostJSON.getJSONArray("vms");
            for(int i = 0; i < vmJSONArray.size(); i++){
                JSONObject vmJSON = vmJSONArray.getJSONObject(i);
                VirtualMachine vm = new VirtualMachine(
                        vmJSON.getString("vm_uuid"),
                        vmJSON.getString("hostname"),
                        vmJSON.getDouble("cgroup_cpu_usage_all_ave"),
                        vmJSON.getInteger("vcpus"),
                        vmJSON.getLong("max_mem")
                );
                snapShotEnvironment.add(host, vm);
            }
        }
        return snapShotEnvironment;
    }
}
