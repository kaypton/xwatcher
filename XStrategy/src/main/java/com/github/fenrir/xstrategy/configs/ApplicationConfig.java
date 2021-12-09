package com.github.fenrir.xstrategy.configs;

import com.github.fenrir.xmessaging.XMessagingConfiguration;
import com.github.fenrir.xstrategy.restapis.ServerTopologyRestAPI;
import com.github.fenrir.xstrategy.restapis.XPlannerRestAPI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class ApplicationConfig {

    private final String natsAddresses;

    public ApplicationConfig(@Value("${XMessaging.NatsAddress}") String natsAddresses,
                             @Value("${XStrategy.XServerTopologyBuilderHost}") String sHost,
                             @Value("${XStrategy.XPlannerHost}") String pHost){
        this.natsAddresses = natsAddresses;
        ServerTopologyRestAPI.setHost(sHost);
        XPlannerRestAPI.setHost(pHost);
    }

    @Bean
    @Scope("prototype")
    public XMessagingConfiguration getXMessagingConfiguration(){
        return new XMessagingConfiguration(this.natsAddresses);
    }
}
