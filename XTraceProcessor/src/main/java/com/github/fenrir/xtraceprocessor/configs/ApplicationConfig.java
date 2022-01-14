package com.github.fenrir.xtraceprocessor.configs;

import com.github.fenrir.xmessaging.XMessaging;
import com.github.fenrir.xmessaging.XMessagingConfiguration;
import com.github.fenrir.xtraceprocessor.services.CollectorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfig {
    static private final Logger LOGGER = LoggerFactory.getLogger(ApplicationConfig.class);

    public XMessaging xMessaging;

    static public String prometheusJobName;

    public ApplicationConfig(@Value("${XServiceDependency.traceData.natsTopic}") String traceDataNatsTopic,
                             @Value("${XMessaging.NatsAddress}") String natsAddress,
                             @Value("${XServiceDependency.prometheusJobName}") String _prometheusJobName){
        prometheusJobName = _prometheusJobName;
        if(!traceDataNatsTopic.equals("none")) {
            CollectorService.natsTopic = traceDataNatsTopic;
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
