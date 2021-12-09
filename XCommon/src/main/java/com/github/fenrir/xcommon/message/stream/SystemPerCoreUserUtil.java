package com.github.fenrir.xcommon.message.stream;

import com.alibaba.fastjson.annotation.JSONField;
import com.github.fenrir.xcommon.message.annotations.Stream;
import lombok.Getter;
import lombok.Setter;

@Stream(name = "system.per.core.user.util", topicName = "stream.system.per.core.user.util",
        description = "per core user util")
public class SystemPerCoreUserUtil extends BaseStream {
    @Override
    protected Object setStreamData(Object... params) {
        StreamData data = new StreamData();
        if(params.length != StreamData.getNum()){
            return null;
        }else{
            data.setHost((String) params[0]);
            data.setValue((double[]) params[1]);
            data.setTimestamp((long[]) params[2]);
        }
        return data;
    }

    public static class StreamData {
        @Getter
        private static final Integer num = 3;

        @JSONField(name="host")
        @Getter @Setter public String host;

        @JSONField(name="value")
        @Getter @Setter public double [] value;

        @JSONField(name="timestamp")
        @Getter @Setter public long[] timestamp;
    }
}
