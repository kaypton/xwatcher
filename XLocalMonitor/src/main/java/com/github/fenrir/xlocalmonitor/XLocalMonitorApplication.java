package com.github.fenrir.xlocalmonitor;

import com.github.fenrir.xcommon.message.annotations.MsgDefinitionScan;
import com.github.fenrir.xlocalmonitor.annotations.InspectorScan;
import com.github.fenrir.xlocalmonitor.annotations.MonitorScan;
import com.github.fenrir.xmessaging.rpc.annotation.RpcServerScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;

@MsgDefinitionScan(
    path = {
            "com.github.fenrir.xcommon.message.event",
            "com.github.fenrir.xcommon.message.stream"
    }
)
@RpcServerScan(path = {"com.github.fenrir.xlocalmonitor.rpc"})
@InspectorScan(path = {
        "com.github.fenrir.xlocalmonitor.inspectors.thirdpart.clients.cadvisorclient",
        "com.github.fenrir.xlocalmonitor.inspectors.thirdpart.clients.netdataclient",
        "com.github.fenrir.xlocalmonitor.inspectors.thirdpart.clients.libvirtclient",
        "com.github.fenrir.xlocalmonitor.inspectors.thirdpart.clients.dockerclient",

        "com.github.fenrir.xlocalmonitor.inspectors.local.memory.linux",
        "com.github.fenrir.xlocalmonitor.inspectors.local.cpu.linux.proc"
})
@MonitorScan(path = {
        "com.github.fenrir.xlocalmonitor.monitors.hybrid",
        "com.github.fenrir.xlocalmonitor.monitors.libvirt",
        "com.github.fenrir.xlocalmonitor.monitors.netdata",
        "com.github.fenrir.xlocalmonitor.monitors.oshi",
        "com.github.fenrir.xlocalmonitor.monitors.docker",
        "com.github.fenrir.xlocalmonitor.monitors.local"
})
@SpringBootApplication(
        exclude = {  // exclude MongoDB auto configuration
                MongoAutoConfiguration.class,
                MongoDataAutoConfiguration.class
        }
)
public class XLocalMonitorApplication {
    public static void main(String[] args){
        SpringApplication.run(XLocalMonitorApplication.class);
    }
}
