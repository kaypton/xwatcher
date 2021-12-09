package com.github.fenrir.xcommon.message.event;

import com.alibaba.fastjson.annotation.JSONField;
import com.github.fenrir.xcommon.message.annotations.Event;
import lombok.Getter;
import lombok.Setter;

/**
 * class {@code SystemCpuOverloadEvent}<br/>
 * 服务器 CPU 过载事件<br/><br/>
 * extra data:<br/>
 * host -> 发生此事件的服务器<br/>
 * cpu_util -> 发生此事件时的 CPU 利用率<br/>
 * timestamp -> 发生此事件的时间戳<br/>
 */
@Event(name = "system.cpu.overload", topicName = "event.system.cpu.overload",
       description = "cpu overload")
public class SystemCpuOverloadEvent extends BaseEvent {

    @Override
    public Object setExtraData(Object... params) {
        if(params.length != ExtraData.getNum()){
            return null;
        }else{
            ExtraData data = new ExtraData();
            data.setHost((String) params[0]);
            data.setCpuUtil((Double) params[1]);
            data.setTimestamp((Long) params[2]);
            return data;
        }
    }

    static public class ExtraData {
        @Getter private static final Integer num = 3;

        @JSONField(name="host")
        @Getter @Setter public String host;

        @JSONField(name="cpu_util")
        @Getter @Setter public double cpuUtil;

        @JSONField(name="timestamp")
        @Getter @Setter public long timestamp;
    }
}
