package com.github.fenrir.xapiserverclient.rest.responseEntities.api.v1;

import com.github.fenrir.xapiserverclient.rest.responseEntities.BaseResponse;

import java.util.ArrayList;
import java.util.List;

public class XLocalMonitorGetResponse extends BaseResponse {

    private final List<XLocalMonitorRecord> records = new ArrayList<>();

    public void addXLocalMonitorRecord(XLocalMonitorRecord record){
        this.records.add(record);
    }

    public List<XLocalMonitorRecord> getRecords() {
        return records;
    }

    public static class XLocalMonitorRecord {
        private String hostname;
        private String ipAddress;
        private String id;
        private String rpcServerTopic;
        private long lastUpdateUnixTimestamp;

        public String getHostname() {
            return hostname;
        }

        public void setHostname(String hostname) {
            this.hostname = hostname;
        }

        public String getIpAddress() {
            return ipAddress;
        }

        public void setIpAddress(String ipAddress) {
            this.ipAddress = ipAddress;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getRpcServerTopic() {
            return rpcServerTopic;
        }

        public void setRpcServerTopic(String rpcServerTopic) {
            this.rpcServerTopic = rpcServerTopic;
        }

        public long getLastUpdateUnixTimestamp() {
            return lastUpdateUnixTimestamp;
        }

        public void setLastUpdateUnixTimestamp(long lastUpdateUnixTimestamp) {
            this.lastUpdateUnixTimestamp = lastUpdateUnixTimestamp;
        }
    }
}
