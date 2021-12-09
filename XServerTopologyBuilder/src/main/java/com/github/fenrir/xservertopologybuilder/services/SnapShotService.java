package com.github.fenrir.xservertopologybuilder.services;

import com.github.fenrir.xservertopologybuilder.services.host.HostTopologyService;
import com.github.fenrir.xservertopologybuilder.services.vm.VMTopologyService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SnapShotService {

    @Getter @Setter private HostTopologyService hostTopologyService;
    @Getter @Setter private VMTopologyService vmTopologyService;

    public SnapShotService(@Autowired HostTopologyService hostTopologyService,
                           @Autowired VMTopologyService vmTopologyService){
        this.setHostTopologyService(hostTopologyService);
        this.setVmTopologyService(vmTopologyService);
    }

    public Map<HostTopologyService.Host, Set<VMTopologyService.VM>> getEntireSnapShot(){
        Map<String, HostTopologyService.Host> hostMap = hostTopologyService.getHostMap();
        Map<String, VMTopologyService.VM> vmMap = vmTopologyService.getVmMap();

        Map<HostTopologyService.Host, Set<VMTopologyService.VM>> topologyMap =
                new ConcurrentHashMap<>();

        for(String hostname : hostMap.keySet()){
            HostTopologyService.Host host = hostMap.get(hostname);
            if(!topologyMap.containsKey(host))
                topologyMap.put(host, new HashSet<>());
            Set<VMTopologyService.VM> vmSet = topologyMap.get(host);
            for(String vmUUID : vmMap.keySet()){
                VMTopologyService.VM vm = vmMap.get(vmUUID);
                if(vm.getHostname().equals(hostname))
                    vmSet.add(vm);
            }
        }

        return topologyMap;
    }
}
