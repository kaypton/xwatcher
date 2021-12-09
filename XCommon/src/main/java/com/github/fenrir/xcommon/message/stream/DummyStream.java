package com.github.fenrir.xcommon.message.stream;

import com.alibaba.fastjson.annotation.JSONField;
import com.github.fenrir.xcommon.message.annotations.Stream;
import lombok.Getter;
import lombok.Setter;

@Stream(name = "dummy", topicName = "stream.dummy", description = "dummy stream")
public class DummyStream extends BaseStream {

    @Override
    protected Object setStreamData(Object... params) {
        SystemCpuSystemUtilStream.StreamData data = new SystemCpuSystemUtilStream.StreamData();
        if(params.length != SystemCpuSystemUtilStream.StreamData.getNum()){
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
