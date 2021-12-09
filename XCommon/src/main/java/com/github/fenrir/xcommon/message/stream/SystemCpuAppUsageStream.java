package com.github.fenrir.xcommon.message.stream;

import com.alibaba.fastjson.annotation.JSONField;
import com.github.fenrir.xcommon.message.annotations.Stream;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Stream(name = "system.cpu.app.usage", topicName = "stream.system.cpu.app.usage",
        description = "cpu application usage")
public class SystemCpuAppUsageStream extends BaseStream {

    @Override
    @SuppressWarnings("unchecked")
    protected Object setStreamData(Object... params){
        StreamData data = new StreamData();
        if(params.length != StreamData.getNum()){
            return null;
        }else{
            data.setHost((String) params[0]);
            data.setApps((List<String>) params[1]);
            data.setValues((List<Double>) params[2]);
            data.setTimestamp((Long) params[3]);
        }
        return data;
    }

    public static final class StreamData {

        @Getter private static final Integer num = 4;

        @JSONField(name="host")
        @Getter @Setter public String host;

        @JSONField(name="apps")
        @Getter @Setter public List<String> apps;

        @JSONField(name="values")
        @Getter @Setter public List<Double> values;

        @JSONField(name="timestamp")
        @Getter @Setter public long timestamp;
    }
}
