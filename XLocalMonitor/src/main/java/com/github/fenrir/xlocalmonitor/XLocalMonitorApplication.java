package com.github.fenrir.xlocalmonitor;

import com.github.fenrir.xlocalmonitor.annotations.InspectorScan;
import com.github.fenrir.xlocalmonitor.annotations.MonitorScan;
import com.github.fenrir.xmessaging.rpc.annotation.RpcServerScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;

@RpcServerScan(path = {"com.github.fenrir.xlocalmonitor.rpc"})
@InspectorScan(path = {
        "com.github.fenrir.xlocalmonitor.inspectors.thirdpart.clients.cadvisorclient",
        "com.github.fenrir.xlocalmonitor.inspectors.thirdpart.clients.libvirtclient",
        "com.github.fenrir.xlocalmonitor.inspectors.thirdpart.clients.dockerclient",

        "com.github.fenrir.xlocalmonitor.inspectors.local.memory.linux",
        "com.github.fenrir.xlocalmonitor.inspectors.local.cpu.linux.proc"
})
@MonitorScan(path = {
        "com.github.fenrir.xlocalmonitor.monitors.docker",
        "com.github.fenrir.xlocalmonitor.monitors.local"
})
@SpringBootApplication(
        exclude = {  // exclude MongoDB auto configuration
                MongoAutoConfiguration.class,
                MongoDataAutoConfiguration.class
        },
        scanBasePackages = {
                "com.github.fenrir.xlocalmonitor",
                "com.github.fenrir.prometheusdata"
        }
)
public class XLocalMonitorApplication {
    private static ConfigurableApplicationContext context;

    public static void main(String[] args){
        context = SpringApplication.run(XLocalMonitorApplication.class);
    }
}
