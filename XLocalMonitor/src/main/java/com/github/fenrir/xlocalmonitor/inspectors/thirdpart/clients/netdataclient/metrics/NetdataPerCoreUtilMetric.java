package com.github.fenrir.xlocalmonitor.inspectors.thirdpart.clients.netdataclient.metrics;

public class NetdataPerCoreUtilMetric extends NetdataBaseMetric {
    public enum Kind {
        USER("user"),
        SYSTEM("system"),
        GUEST("guest"),
        IOWAIT("iowait");

        private final String kind;

        private Kind(String kind){
            this.kind = kind;
        }
        public String getKindString(){
            return this.kind;
        }
    }
}
