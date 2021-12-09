package com.github.fenrir.xservertopologybuilder.services.vm;

import com.alibaba.fastjson.JSON;
import com.github.fenrir.xcommon.message.stream.VMInfoStream;
import com.github.fenrir.xmessaging.XMessage;
import com.github.fenrir.xmessaging.MessageProcessCallBack;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class VMInfoSyncer {
    private static class StreamListener implements MessageProcessCallBack {
        @Getter @Setter private VMTopologyService service;

        public StreamListener(VMTopologyService service){
            this.setService(service);
        }
        @Override
        public void processMessage(XMessage data) {
            VMInfoStream.StreamData streamData =
                    JSON.parseObject(data.getStringData(), VMInfoStream.StreamData.class);
            service.report(
                    streamData.getVmUUID(),
                    "vm.name",
                    streamData.getVmName()
            );

            service.report(
                    streamData.getVmUUID(),
                    "vm.vcpus",
                    streamData.getVCPUNum()
            );

            service.report(
                    streamData.getVmUUID(),
                    "vm.max_mem",
                    streamData.getVMEMSize()
            );

            service.report(
                    streamData.getVmUUID(),
                    "vm.uuid",
                    streamData.getVmUUID()
            );

            service.report(
                    streamData.getVmUUID(),
                    "hostname",
                    streamData.getHost()
            );

            service.report(
                    streamData.getVmUUID(),
                    "cgroup_cpu_usage_user",
                    streamData.getCgroupCpuUsageUser()
            );

            service.report(
                    streamData.getVmUUID(),
                    "cgroup_cpu_usage_system",
                    streamData.getCgroupCpuUsageSystem()
            );
        }
    }

    public VMInfoSyncer(@Autowired VMTopologyService vmTopologyService){
    }
}
