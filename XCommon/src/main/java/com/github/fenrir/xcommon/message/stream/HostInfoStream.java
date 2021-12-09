package com.github.fenrir.xcommon.message.stream;

import com.alibaba.fastjson.annotation.JSONField;
import com.github.fenrir.xcommon.message.annotations.Stream;
import lombok.Getter;
import lombok.Setter;

@Stream(name = "host.info", topicName = "stream.host.info", description = "physical machine information")
public class HostInfoStream extends BaseStream {

    @Override
    protected Object setStreamData(Object... params){
        StreamData data = new StreamData();
        if(params.length != StreamData.getNum()){
            return null;
        }else{
            data.setHost((String) params[0]);
            data.setCpuCoreNum((Integer) params[1]);
            data.setMemoryTotalKb((Long) params[2]);
            data.setTimestamp((Long) params[3]);
        }
        return data;
    }

    public static final class StreamData {
        @Getter private final static Integer num = 4;

        @JSONField(name="host")
        @Getter @Setter public String host;

        @JSONField(name="cpu_core_num")
        @Getter @Setter public Integer cpuCoreNum;

        @JSONField(name="mem_total_kb")
        @Getter @Setter public Long memoryTotalKb;

        @JSONField(name="timestamp")
        @Getter @Setter public long timestamp;
    }
}
