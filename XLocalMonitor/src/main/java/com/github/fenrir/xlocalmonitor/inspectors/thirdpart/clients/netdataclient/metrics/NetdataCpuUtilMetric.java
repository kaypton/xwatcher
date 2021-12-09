package com.github.fenrir.xlocalmonitor.inspectors.thirdpart.clients.netdataclient.metrics;

import com.alibaba.fastjson.JSONArray;
import lombok.Getter;
import lombok.Setter;

public class NetdataCpuUtilMetric extends NetdataBaseMetric {

    public enum Kind {
        USER("user"),
        SYSTEM("system"),
        GUEST("guest"),
        IOWAIT("iowait");

        private final String kind;

        private Kind(String kind){
            this.kind = kind;
        }

        @Override
        public String toString(){
            return this.kind;
        }
    }
}
