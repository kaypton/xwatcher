package com.github.fenrir.xcommon.clients.xregistry.api.rest;

import com.github.fenrir.xcommon.clients.xlocalmonitor.types.LocalMonitorType;
import com.github.fenrir.xcommon.clients.xregistry.types.rest.RegisterMessageBuilder;
import com.github.fenrir.xcommon.clients.xregistry.types.rest.RegisterResponseMessage;
import com.github.fenrir.xcommon.clients.xregistry.types.rest.RegisterResponseMessageBuilder;
import com.github.fenrir.xcommon.clients.xregistry.types.rest.constants.RegisterStatus;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

public class XRegistryRestAPI {

    private final RestTemplate restTemplate;
    private final String host;

    private final String registerURI = "/api/v1/localmonitor/register";
    private final String keepaliveURI = "/api/v1/localmonitor/keepalive";
    private final String getAllLocalMonitorURI = "/api/v1/localmonitor/info/all";
    private final String deleteLocalMonitorURI = "/api/v1/localmonitor/delete";

    public XRegistryRestAPI(RestTemplate restTemplate,
                            String host){
        this.restTemplate = restTemplate;
        this.host = host;
    }

    public RegisterResponseMessage register(String hostname,
                                            LocalMonitorType localMonitorType,
                                            String bindAddress){

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(
                RegisterMessageBuilder.builder()
                        .setHostname(hostname)
                        .setIpAddr(bindAddress)
                        .setLocalMonitorType(localMonitorType)
                        .build().toJSONString(),
                        httpHeaders);

        try{
            String response = this.restTemplate.postForObject(
                    "http://" + this.host + registerURI,
                    entity,
                    String.class
            );
            return RegisterResponseMessageBuilder.builder()
                    .fromJSONString(response);
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public String getAllLocalMonitor(){
        return this.restTemplate.getForObject(
                "http://" + this.host + this.getAllLocalMonitorURI,
                String.class
        );
    }

    public String deleteLocalMonitorById(String id){
        return this.restTemplate.getForObject(
                "http://" + this.host + this.deleteLocalMonitorURI + "?id=" + id,
                String.class
        );
    }
}
