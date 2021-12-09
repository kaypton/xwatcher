package com.github.fenrir.xstrategy.restapis;

import com.alibaba.fastjson.JSONObject;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class ServerTopologyRestAPI {
    @Getter @Setter private static String host;

    @Getter public final static String rootURL = "/server_topology";
    @Getter public final static String HTTP = "http://";
    @Getter public final static String HTTPS = "https://";

    @Getter public final RestTemplate restTemplate = new RestTemplate();

    public ServerTopologyRestAPI(){

    }

    public String getAllHosts(){
        String URL = HTTP + getHost() + rootURL + "/all_hosts";
        return this.getRestTemplate().getForObject(
                URL, String.class
        );
    }

    public String getAllVMs(){
        String URL = HTTP + getHost() + rootURL + "/all_vms";
        return this.getRestTemplate().getForObject(
                URL, String.class
        );
    }

    public String getVmsByHostname(List<String> hostnames){
        String URL = HTTP + getHost() + rootURL + "/conditional_search/vm/get_vm_from_host";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(
                MediaType.parseMediaType(
                        "application/json; charset=UTF-8"
                )
        );
        headers.add("Accept", MediaType.APPLICATION_JSON.toString());

        JSONObject payload = new JSONObject();
        payload.put("hosts", hostnames);

        return this.getRestTemplate().postForObject(
                URL,
                new HttpEntity<>(payload.toJSONString(), headers),
                String.class
        );
    }

    public String getHostCpuGuestUtilHigherThan(Double threshold,
                                                Boolean equal){
        String URL = HTTP + getHost() + rootURL +
                "/conditional_search/host/cpu_guest_util/higher_than";
        URL += "?threshold=" + threshold + "&equal=" + equal;

        return this.getRestTemplate().getForObject(
                URL, String.class
        );
    }

    public String getHostCpuGuestUtilLessThan(Double threshold,
                                              Boolean equal){
        String URL = HTTP + getHost() + rootURL +
                "/conditional_search/host/cpu_guest_util/less_than";
        URL += "?threshold=" + threshold + "&equal=" + equal;

        return this.getRestTemplate().getForObject(
                URL, String.class
        );
    }

    public String getEntireSnapShot(){
        String URL = HTTP + getHost() + rootURL +
                "/snapshot/entire";
        return this.getRestTemplate().getForObject(
                URL, String.class
        );
    }
}
