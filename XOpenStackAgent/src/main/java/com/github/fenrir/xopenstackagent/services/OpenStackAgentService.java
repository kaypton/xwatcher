package com.github.fenrir.xopenstackagent.services;

import lombok.Getter;
import lombok.Setter;
import org.openstack4j.api.OSClient;
import org.openstack4j.model.common.Identifier;
import org.openstack4j.model.compute.Server;
import org.openstack4j.model.identity.v3.Endpoint;
import org.openstack4j.model.storage.block.Volume;
import org.openstack4j.openstack.OSFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OpenStackAgentService {
    @Getter @Setter private static String keystoneAddress = null;
    @Getter @Setter private static String username = null;
    @Getter @Setter private static String domainName = null;
    @Getter @Setter private static String password = null;
    @Getter @Setter private static String projectUUID = null;

    public OSClient.OSClientV3 getOSClient(){
        return OSFactory.builderV3()
                .endpoint(keystoneAddress)
                .credentials(username, password, Identifier.byName(domainName))
                .scopeToProject(Identifier.byId(projectUUID))
                .authenticate();
    }
}
