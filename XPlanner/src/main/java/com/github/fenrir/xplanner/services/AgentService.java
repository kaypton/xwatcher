package com.github.fenrir.xplanner.services;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import com.github.fenrir.xcommon.actions.Action;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AgentService {
    @Getter @Setter private static Map<String, String> agentHosts = new ConcurrentHashMap<>();

    public static class AgentExecuteResult {
        @JSONField(name="status")
        @Getter @Setter private String status;

        @JSONField(name="message")
        @Getter @Setter private String message;
    }

    public AgentService(){

    }

    private RestTemplate getRestTemplate(){
        return new RestTemplate();
    }

    public AgentExecuteResult executeAction(Action<Object> action){
        String actionJSONString = action.toJSONString();
        String URL = "http://" + agentHosts.get(action.agentName) + "/planner/doAction";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(
                MediaType.parseMediaType(
                        "application/json;charset=UTF-8"
                )
        );
        headers.add("Accept", MediaType.APPLICATION_JSON.toString());

        String result = this.getRestTemplate().postForObject(
                URL, new HttpEntity<>(actionJSONString, headers),
                String.class
        );

        return JSON.parseObject(result, AgentExecuteResult.class);
    }

    static public void addAgent(String name, String address){
        agentHosts.put(name, address);
    }
}
