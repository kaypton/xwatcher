package com.github.fenrir.xlocalmonitor.configs;

import com.github.fenrir.xapiserverclient.rest.XApiServerRestClient;
import com.github.fenrir.xapiserverclient.rest.responseEntities.api.v1.XLocalMonitorUpdateResponse;
import com.github.fenrir.xcommon.clients.xlocalmonitor.types.LocalMonitorType;
import com.github.fenrir.xlocalmonitor.XLocalMonitorApplication;
import com.github.fenrir.xlocalmonitor.entities.MessageEntityFactory;
import com.github.fenrir.xlocalmonitor.inspectors.thirdpart.clients.dockerclient.DockerAPI;
import com.github.fenrir.xlocalmonitor.inspectors.thirdpart.clients.libvirtclient.LibvirtAPI;
import com.github.fenrir.xlocalmonitor.services.monitor.XLocalMonitorExecutor;
import com.github.fenrir.xlocalmonitor.services.monitor.XLocalMonitorFactory;
import com.github.fenrir.xmessaging.XMessaging;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Configuration
public class ApplicationConfig {
    static private final Logger LOGGER = LoggerFactory.getLogger("ApplicationConfig");

    static public String[] enableMonitors;

    public ApplicationConfig(@Value("${XLocalMonitor.hostnameOverride}") String hostname,
                             @Value("${XLocalMonitor.xapiserver}") String xApiServerAddress,
                             @Value("${XLocalMonitor.type}") String localMonitorTypeStr,
                             @Value("${XLocalMonitor.address}") String bindAddress,
                             @Value("${XMessaging.NatsAddress}") String natsAddress,
                             @Value("${Monitors.Libvirt.connection}") String libvirtConnection,
                             @Value("${Monitors.Docker.host}") String dockerDaemonHost,
                             @Value("${Monitors.Run.monitors}") String[] _enableMonitors,
                             @Value("${XLocalMonitor.messages}") String messagesPath){

        LibvirtAPI.setLibvirtConnectionURL(libvirtConnection);
        DockerAPI.setHost(dockerDaemonHost);

        try{
            MessageEntityFactory.init(messagesPath);
        } catch (Exception e){
            e.printStackTrace();
        }

        if(_enableMonitors != null) {
            StringBuilder builder = new StringBuilder();
            builder.append("Enable monitors: ");
            for(String enableMonitor : _enableMonitors){
                builder.append(enableMonitor).append(" ");
            }
            LOGGER.info(builder.toString());
            enableMonitors = _enableMonitors;
        }

        assert localMonitorTypeStr != null;
        assert bindAddress != null;

        LOGGER.info("NATS address      : " + natsAddress);
        LOGGER.info("LocalMonitor type : " + localMonitorTypeStr);
        LOGGER.info("XApiServer address : " + xApiServerAddress);
        LOGGER.info("Bind address      : " + bindAddress);

        if(natsAddress == null){
            LOGGER.error("NATS address is null");
            System.exit(-1);
        }

        if(xApiServerAddress == null){
            LOGGER.error("XApiServer address is null");
            System.exit(-1);
        }

        XApiServerRestClient xApiServerRestClient = XApiServerRestClient.builder()
                .host(xApiServerAddress)
                .build();

        LocalMonitorType localMonitorType = LocalMonitorType.from(localMonitorTypeStr);

        XLocalMonitorUpdateResponse xLocalMonitorUpdateResponse = null;
        int count = 1;
        do{
            LOGGER.info("Try connect to XApiServer {} time. ", count);
            xLocalMonitorUpdateResponse = xApiServerRestClient.getXLocalMonitorClient().update(hostname, bindAddress);

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            count++;
        }while(xLocalMonitorUpdateResponse == null);

        String rpcServerTopic = xLocalMonitorUpdateResponse.getRpcServerTopic();

        XMessaging.init(natsAddress);

        try {
            XLocalMonitorFactory.init(XLocalMonitorApplication.class,
                    hostname == null ? InetAddress.getLocalHost().getHostName() : hostname,
                    rpcServerTopic);
            XLocalMonitorExecutor.startup();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            LOGGER.error(e.getMessage());
            System.exit(-1);
        }



        XMessaging.initRpc(XLocalMonitorApplication.class,
                xLocalMonitorUpdateResponse.getRpcServerTopic(),
                natsAddress);
    }
}
