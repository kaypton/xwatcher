package com.github.fenrir.xapiserverclient.rest.v1;

import com.alibaba.fastjson.JSON;
import com.github.fenrir.xapiserverclient.rest.responseEntities.api.v1.XLocalMonitorUpdateResponse;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class XLocalMonitorRestClient {

    private final String prefix;
    private final RestTemplate restTemplate;

    private static final String UPDATE_URL = "/api/v1/xlocalmonitor/update/";

    public XLocalMonitorRestClient(String prefix){
        this.prefix = prefix;
        this.restTemplate = new RestTemplate();
    }

    public XLocalMonitorUpdateResponse update(String hostname, String ipAddress){
        String URL = prefix + UPDATE_URL + hostname + "/" + ipAddress;

        HttpHeaders headers = new HttpHeaders();
        headers.add("user-agent", "XApiServerRestClient.XLocalMonitorClient");

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(URL, HttpMethod.PUT, entity, String.class);
        return JSON.parseObject(response.getBody(), XLocalMonitorUpdateResponse.class);
    }
}
