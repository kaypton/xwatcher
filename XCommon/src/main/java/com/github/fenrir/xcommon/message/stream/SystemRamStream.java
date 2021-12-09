package com.github.fenrir.xcommon.message.stream;

import com.alibaba.fastjson.annotation.JSONField;
import com.github.fenrir.xcommon.message.annotations.Stream;
import lombok.Getter;
import lombok.Setter;

// TODO
@Stream(name = "system.ram", topicName = "stream.system.ram",
        description = "ram info")
public class SystemRamStream extends BaseStream {

    @Override
    protected Object setStreamData(Object... params) {
        StreamData data = new StreamData();
        if(params.length != StreamData.getNum()){
            return null;
        }else{
            data.setHost((String) params[0]);
            data.setMemTotal((Long) params[1]);
            data.setMemFree((Long) params[2]);
            data.setMemAvailable((Long) params[3]);
            data.setTimestamp((Long) params[4]);
        }
        return data;
    }

    public static class StreamData {
        @Getter
        private static final Integer num = 5;

        @JSONField(name="host")
        @Getter @Setter public String host;

        @JSONField(name="mem.total")
        @Getter @Setter public long memTotal;

        @JSONField(name="mem.free")
        @Getter @Setter public long memFree;

        @JSONField(name="mem.available")
        @Getter @Setter public long memAvailable;

        @JSONField(name="timestamp")
        @Getter @Setter public long timestamp;
    }
}
