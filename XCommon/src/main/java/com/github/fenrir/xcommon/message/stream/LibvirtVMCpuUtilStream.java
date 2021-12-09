package com.github.fenrir.xcommon.message.stream;

import com.alibaba.fastjson.annotation.JSONField;
import com.github.fenrir.xcommon.message.annotations.Stream;
import lombok.Getter;
import lombok.Setter;

@Stream(name = "libvirt.vm.cpu.util", topicName = "stream.libvirt.vm.cpu.util",
        description = "libvirt vm cpu util")
public class LibvirtVMCpuUtilStream extends BaseStream {

    @Override
    protected Object setStreamData(Object... params){
        StreamData data = new StreamData();
        if(params.length != StreamData.getNum()){
            return null;
        }else{
            data.setHost((String) params[0]);
            data.setVmid((Integer) params[1]);
            data.setValue((Double) params[2]);
            data.setTimestamp((Long) params[3]);
            return data;
        }
    }

    public static final class StreamData {
        @Getter private static final Integer num = 3;

        @JSONField(name="host")
        @Getter @Setter private String host;

        @JSONField(name="vmid")
        @Getter @Setter private Integer vmid;

        @JSONField(name="value")
        @Getter @Setter private Double value;

        @JSONField(name="timestamp")
        @Getter @Setter private Long timestamp;
    }
}
