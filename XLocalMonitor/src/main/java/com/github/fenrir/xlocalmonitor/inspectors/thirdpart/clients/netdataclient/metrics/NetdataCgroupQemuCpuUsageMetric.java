package com.github.fenrir.xlocalmonitor.inspectors.thirdpart.clients.netdataclient.metrics;

import com.alibaba.fastjson.JSONArray;
import lombok.Getter;
import lombok.Setter;

public class NetdataCgroupQemuCpuUsageMetric extends NetdataBaseMetric {
    public enum Kind {
        USER("user"),
        SYSTEM("system");

        private final String kind;

        private Kind(String kind){
            this.kind = kind;
        }
        public String getKindString(){
            return this.kind;
        }
    }
}
