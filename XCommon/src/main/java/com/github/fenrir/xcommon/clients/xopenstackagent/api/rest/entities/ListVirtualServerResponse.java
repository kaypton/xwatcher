package com.github.fenrir.xcommon.clients.xopenstackagent.api.rest.entities;

import java.util.List;

public class ListVirtualServerResponse {
    public static class Server {
        public String serverId;
        public String hostId;
        public String host;
        public String hypervisorHostname;
        public String createdDate;
    }

    public List<Server> servers;
    public int serverNum;
}
