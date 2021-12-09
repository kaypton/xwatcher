package com.github.fenrir.xcommon.message.stream;

import com.alibaba.fastjson.annotation.JSONField;
import com.github.fenrir.xcommon.message.annotations.Stream;
import lombok.Getter;
import lombok.Setter;

@Stream(name = "vm.info", topicName = "stream.vm.info",
        description = "virtual machine information")
public class VMInfoStream extends BaseStream {

    @Override
    protected Object setStreamData(Object... params){
        StreamData data = new StreamData();
        if(params.length != StreamData.getNum()){
            return null;
        }else{
            data.setHost((String) params[0]);
            data.setVmName((String) params[1]);
            data.setVmUUID((String) params[2]);
            data.setVCPUNum((Integer) params[3]);
            data.setVMEMSize((Long) params[4]);
            data.setCgroupCpuUsageUser((Double) params[5]);
            data.setCgroupCpuUsageSystem((Double) params[6]);
            data.setTimestamp((Long) params[8]);
        }
        return data;
    }

    public static final class StreamData {
        @Getter private static final Integer num = 8;

        @JSONField(name="host")
        @Getter @Setter public String host;

        @JSONField(name="vmName")
        @Getter @Setter public String vmName;

        @JSONField(name="vmUUID")
        @Getter @Setter public String vmUUID;

        @JSONField(name="vCPUNum")
        @Getter @Setter public int vCPUNum;

        @JSONField(name="vMEMSize")
        @Getter @Setter public long vMEMSize;

        @JSONField(name="cgroup_cpu_usage_user")
        @Getter @Setter public double cgroupCpuUsageUser;

        @JSONField(name="cgroup_cpu_usage_system")
        @Getter @Setter public double cgroupCpuUsageSystem;

        @JSONField(name="timestamp")
        @Getter @Setter public long timestamp;
    }
}
