package com.github.fenrir.xlocalmonitor.inspectors.thirdpart.clients.netdataclient.metrics;

import com.alibaba.fastjson.JSONArray;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

public class NetdataCpuAppUsageMetric extends NetdataBaseMetric {

    public List<String> getAppList(){
        List<String> apps = new ArrayList<>();
        String[] labels = this.getLabels();
        for(String appName : labels){
            if(!appName.equals("time"))
                apps.add(appName);
        }
        return apps;
    }
}
