package com.github.fenrir.xlocalmonitor.inspectors.thirdpart.clients.netdataclient.metrics;

import com.alibaba.fastjson.JSONArray;
import lombok.Getter;
import lombok.Setter;

public class NetdataMemUtilMetric extends NetdataBaseMetric{

    public enum Kind {

        FREE("free"),
        USED("used"),
        CACHED("cached"),
        BUFFERS("buffers");

        private final String kind;

        private Kind(String kind){
            this.kind = kind;
        }
        public String getKindString(){
            return this.kind;
        }
    }
}
