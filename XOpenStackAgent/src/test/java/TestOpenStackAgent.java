import com.github.fenrir.xopenstackagent.services.OpenStackAgentService;
import org.junit.Test;
import org.openstack4j.model.compute.Server;
import org.openstack4j.model.identity.v3.Endpoint;
import org.openstack4j.model.storage.block.Volume;

import java.util.List;

public class TestOpenStackAgent {
    /*@Test
    public void testListServers(){
        OpenStackAgentService service;
        OpenStackAgentService.setDomainName("Default");
        OpenStackAgentService.setKeystoneAddress("http://125.216.243.6:5000/v3/");
        OpenStackAgentService.setUsername("admin");
        OpenStackAgentService.setPassword("admin");
        OpenStackAgentService.setProjectUUID("045e01727ae042e6bca295c5ee3671a2");

        service = new OpenStackAgentService();
        List<? extends Server> servers = service.listVirtualServers();
        for(Server server : servers){
            System.out.println(server.getName());
        }
    }

    @Test
    public void testListVolumes() {
        OpenStackAgentService service;
        OpenStackAgentService.setDomainName("Default");
        OpenStackAgentService.setKeystoneAddress("http://125.216.243.6:5000/v3/");
        OpenStackAgentService.setUsername("admin");
        OpenStackAgentService.setPassword("admin");
        OpenStackAgentService.setProjectUUID("045e01727ae042e6bca295c5ee3671a2");

        service = new OpenStackAgentService();
        List<? extends Volume> volumes = service.listVolumes();
        for (Volume volume : volumes) {
            System.out.println(volume.getId());
        }
    }

    @Test
    public void testListEndpoint(){
        OpenStackAgentService service;
        OpenStackAgentService.setDomainName("Default");
        OpenStackAgentService.setKeystoneAddress("http://125.216.243.6:5000/v3/");
        OpenStackAgentService.setUsername("admin");
        OpenStackAgentService.setPassword("admin");
        OpenStackAgentService.setProjectUUID("045e01727ae042e6bca295c5ee3671a2");

        service = new OpenStackAgentService();
        List<? extends Endpoint> endpoints = service.listEndpoint();
        for(Endpoint endpoint : endpoints){
            System.out.println(endpoint.getUrl().toString());
        }
    }*/
}
