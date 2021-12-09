package com.github.fenrir.xlocalmonitor.inspectors.thirdpart.clients.netdataclient;

import com.alibaba.fastjson.JSON;
import com.github.fenrir.xlocalmonitor.annotations.Inspector;
import com.github.fenrir.xlocalmonitor.inspectors.thirdpart.clients.netdataclient.metrics.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Inspector(name = "netdata")
public class NetdataAPI {
    @Getter public final RestTemplate restTemplate = new RestTemplate();

    @Getter String example = "http://222.201.144.196:19999/api/v1/data?chart=system.cpu&group=average&format=json&options=abs&after=-1";
    @Getter @Setter private static String host = null;

    private static class DataQueryURL {
        @Getter private final String path = "/api/v1/data";

        @Getter @Setter private String host = "127.0.0.1:19999";
        @Getter @Setter private Boolean https = false;
        @Getter @Setter private String chart = null;
        @Getter @Setter private String format = null;
        @Getter @Setter private String group = null;
        @Getter @Setter private List<String> options = new ArrayList<>();
        @Getter @Setter private Integer after = null;
        @Getter @Setter private Integer before = null;

        public String getURL(){
            StringBuilder builder = new StringBuilder();
            if(this.getHttps()){
                builder.append("https://");
            }else builder.append("http://");

            if(this.getHost() == null) return null;
            else{
                builder.append(this.getHost());
            }

            builder.append(path).append("?");

            assert this.getChart() != null;

            builder.append("chart=").append(this.getChart());

            if(this.getFormat() == null){
                builder.append("&format=json");
            }else builder.append("&format=").append(this.getFormat());

            if(this.getGroup() == null){
                builder.append("&group=average");
            }else builder.append("&group=").append(this.getGroup());

            if(this.getAfter() != null){
                builder.append("&after=").append(this.getAfter());
            }

            if(this.getBefore() != null){
                builder.append("&before=").append(this.getBefore());
            }

            if(this.getOptions().size() != 0){
                builder.append("&options=");
                for(int index = 0; index < this.getOptions().size(); index++){
                    builder.append(this.getOptions().get(index));
                    if(index < this.getOptions().size() - 1){
                        builder.append(",");
                    }
                }
            }
            return builder.toString();
        }
    }

    public NetdataCpuUtilMetric getCpuUtil(){
        DataQueryURL url = new DataQueryURL();

        url.setHost(getHost());
        url.setChart("system.cpu");
        url.setAfter(-10);
        url.setHttps(false);
        url.getOptions().add("abs");

        if(url.getURL() == null) return null;

        String result = this.getRestTemplate().getForObject(
                url.getURL(),
                String.class
        );

        return JSON.parseObject(result, NetdataCpuUtilMetric.class);
    }

    public NetdataMemUtilMetric getMemUtil(){
        DataQueryURL url = new DataQueryURL();

        url.setHttps(false);
        url.setHost(getHost());
        url.setChart("system.ram");
        url.setAfter(-10);
        url.getOptions().add("abs");

        if(url.getURL() == null) return null;

        String result = this.getRestTemplate().getForObject(
                url.getURL(),
                String.class
        );
        
        return JSON.parseObject(result, NetdataMemUtilMetric.class);
    }

    public NetdataCgroupQemuCpuUsageMetric getCgroupQemuCpuUsage(String instanceName){
        DataQueryURL url = new DataQueryURL();

        String[] instanceNameUnit = instanceName.split("-");
        StringBuilder stringBuilder = new StringBuilder();
        for(String unit : instanceNameUnit){
            stringBuilder.append(unit);
        }

        url.setHttps(false);
        url.setHost(getHost());
        url.setChart("cgroup_qemu_" + stringBuilder.toString() + ".cpu");
        url.setAfter(-10);
        url.getOptions().add("abs");

        if(url.getURL() == null) return null;

        String result = this.getRestTemplate().getForObject(
                url.getURL(), String.class
        );

        return JSON.parseObject(result, NetdataCgroupQemuCpuUsageMetric.class);
    }

    public NetdataCpuAppUsageMetric getCpuAppUsageMetric(){
        DataQueryURL url = new DataQueryURL();

        url.setHttps(false);
        url.setHost(getHost());
        url.setChart("apps.cpu");
        url.setAfter(-10);
        url.getOptions().add("abs");

        if(url.getURL() == null) return null;

        String result = this.getRestTemplate().getForObject(
                url.getURL(), String.class
        );

        return JSON.parseObject(result, NetdataCpuAppUsageMetric.class);
    }

    public NetdataCgroupQemuCpuUsagePerCore getCgroupQemuCpuUsagePerCore(String instanceName){
        DataQueryURL url = new DataQueryURL();

        String[] instanceNameUnit = instanceName.split("-");
        StringBuilder stringBuilder = new StringBuilder();
        for(String unit : instanceNameUnit){
            stringBuilder.append(unit);
        }

        url.setHttps(false);
        url.setHost(getHost());
        url.setChart("cgroup_qemu_" + stringBuilder.toString() + ".cpu_per_core");
        url.setAfter(-10);
        url.getOptions().add("abs");

        if(url.getURL() == null) return null;

        String result = this.getRestTemplate().getForObject(
                url.getURL(), String.class
        );

        return JSON.parseObject(result, NetdataCgroupQemuCpuUsagePerCore.class);
    }

    public NetdataPerCoreUtilMetric getPerCoreUtil(int coreIndex){
        DataQueryURL url = new DataQueryURL();

        url.setHttps(false);
        url.setHost(getHost());
        url.setChart("cpu.cpu" + String.valueOf(coreIndex));
        url.setAfter(-10);
        url.getOptions().add("abs");

        if(url.getURL() == null) return null;

        String result = this.getRestTemplate().getForObject(
                url.getURL(), String.class
        );

        return JSON.parseObject(result, NetdataPerCoreUtilMetric.class);
    }
}
