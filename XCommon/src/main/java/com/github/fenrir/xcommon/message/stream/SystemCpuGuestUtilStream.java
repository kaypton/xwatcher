package com.github.fenrir.xcommon.message.stream;

import com.alibaba.fastjson.annotation.JSONField;
import com.github.fenrir.xcommon.message.annotations.Stream;
import lombok.Getter;
import lombok.Setter;

// percentage
@Stream(name = "system.cpu.guest.util", topicName = "stream.system.cpu.guest.util",
        description = "cpu guest util")
public class SystemCpuGuestUtilStream extends BaseStream {

    @Override
    protected Object setStreamData(Object... params){
        StreamData data = new StreamData();
        if(params.length != StreamData.getNum()){
            return null;
        }else{
            data.setHost((String) params[0]);
            data.setValue((Double) params[1]);
            data.setTimestamp((Long) params[2]);
        }
        return data;
    }

    public static final class StreamData {
        @Getter private static final Integer num = 3;

        @JSONField(name="host")
        @Getter @Setter public String host;

        @JSONField(name="value")
        @Getter @Setter public double value;

        @JSONField(name="timestamp")
        @Getter @Setter public long timestamp;
    }
}
