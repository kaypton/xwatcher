package com.github.fenrir.xplanner.configs;

import com.github.fenrir.xplanner.services.AgentService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfig {
    public ApplicationConfig(@Value("${XPlanner.agentHosts}") String agentHosts){
        String[] agentHostsSplit = agentHosts.split(";");
        for(String tmp : agentHostsSplit){
            String[] s = tmp.split("=");
            AgentService.addAgent(s[0], s[1]);
        }
    }
}
