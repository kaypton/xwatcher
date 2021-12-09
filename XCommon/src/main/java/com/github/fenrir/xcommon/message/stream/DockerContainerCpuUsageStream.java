package com.github.fenrir.xcommon.message.stream;

import com.alibaba.fastjson.annotation.JSONField;
import com.github.fenrir.xcommon.message.annotations.Stream;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Stream(name = "docker.cpu.usage",
        topicName = "stream.docker.cpu.usage",
        description = "docker cpu usage stream")
public class DockerContainerCpuUsageStream extends BaseStream {

    @Override
    @SuppressWarnings("unchecked")
    protected Object setStreamData(Object... params){
        StreamData data = new StreamData();
        if(params.length != StreamData.getNum()){
            return null;
        }else{
            data.setHost((String) params[0]);
            data.setTotalCpuUsage((Map<String, Double>) params[1]);
            data.setTimestamp((Long) params[2]);
        }
        return data;
    }

    public static final class StreamData {
        @Getter private final static Integer num = 3;

        @JSONField(name="host")
        @Getter @Setter public String host;

        @JSONField(name="total_cpu_usage")
        @Getter @Setter public Map<String, Double> totalCpuUsage;

        @JSONField(name="timestamp")
        @Getter @Setter public long timestamp;
    }
}
