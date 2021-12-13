package com.github.fenrir.xservicedependency.configs;

import com.github.fenrir.xmessaging.XMessaging;
import com.github.fenrir.xmessaging.XMessagingConfiguration;
import com.github.fenrir.xservicedependency.services.ReceiveService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfig {
    static private final Logger LOGGER = LoggerFactory.getLogger(ApplicationConfig.class);

    public XMessaging xMessaging = null;

    public ApplicationConfig(@Value("${XServiceDependency.traceData.natsTopic}") String traceDataNatsTopic,
                             @Value("${XMessaging.NatsAddress}") String natsAddress){
        if(!traceDataNatsTopic.equals("none")) {
            ReceiveService.natsTopic = traceDataNatsTopic;
            LOGGER.info("trace data nats topic:{}", traceDataNatsTopic);
        }

        XMessagingConfiguration configuration = new XMessagingConfiguration(natsAddress);
        this.xMessaging = XMessaging.create(configuration);
        LOGGER.info("nats addresses:{}", natsAddress);
    }

    @Bean(name = "xmessaging")
    public XMessaging getXMessaging(){
        return this.xMessaging;
    }
}
