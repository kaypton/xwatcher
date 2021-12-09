package com.github.fenrir.xcommon.message.event;

import com.alibaba.fastjson.annotation.JSONField;
import com.github.fenrir.xcommon.message.annotations.Event;
import lombok.Getter;
import lombok.Setter;

@Event(name = "dummy", topicName = "event.dummy", description = "dummy event")
public class DummyEvent extends BaseEvent {

    @Override
    public Object setExtraData(Object... params) {
        if(params.length != ExtraData.getNum()){
            return null;
        }else{
            ExtraData data = new ExtraData();
            data.setHost((String) params[0]);
            data.setTimestamp((Long) params[1]);
            return data;
        }
    }

    static public class ExtraData {
        @Getter private static final Integer num = 2;

        @JSONField(name="host")
        @Getter @Setter public String host;

        @JSONField(name="timestamp")
        @Getter @Setter public long timestamp;
    }
}
