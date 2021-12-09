package com.github.fenrir.xopenstackagent.services;

import com.github.fenrir.xcommon.clients.xopenstackagent.api.rest.entities.ListVirtualServerResponse;
import lombok.Getter;
import lombok.Setter;
import org.openstack4j.model.compute.Server;
import org.openstack4j.model.identity.v3.Endpoint;
import org.openstack4j.model.storage.block.Volume;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class OpenStackListingService {
    @Getter @Setter private OpenStackAgentService agentService;
    public OpenStackListingService(@Autowired OpenStackAgentService agentService){
        this.setAgentService(agentService);
    }

    public ListVirtualServerResponse listVirtualServers(){
        ListVirtualServerResponse response = new ListVirtualServerResponse();
        response.servers = new ArrayList<>();
        List<? extends Server> servers = this.getAgentService().getOSClient().compute().servers().list();
        response.serverNum = servers.size();
        for(Server server : servers){
            ListVirtualServerResponse.Server _server = new ListVirtualServerResponse.Server();
            _server.serverId = server.getId();
            _server.hostId = server.getHostId();
            _server.host = server.getHost();
            _server.hypervisorHostname = server.getHypervisorHostname();
            _server.createdDate = server.getCreated().toString();
            response.servers.add(_server);
        }
        return response;
    }

    public List<? extends Volume> listVolumes() {
        return this.getAgentService().getOSClient().blockStorage().volumes().list();
    }

    public List<? extends Endpoint> listEndpoint(){
        return this.getAgentService().getOSClient().identity().serviceEndpoints().listEndpoints();
    }
}
