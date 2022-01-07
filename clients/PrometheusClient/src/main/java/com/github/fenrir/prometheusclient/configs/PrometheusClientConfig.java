package com.github.fenrir.prometheusclient.configs;

import com.github.fenrir.xhttpclient.Client;
import com.github.fenrir.xhttpclient.impl.httpClient.XHttpClient;
import io.prometheus.client.exporter.PushGateway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class PrometheusClientConfig {

    private final String prometheusHost;
    private final String pushGatewayHost;

    public PrometheusClientConfig(@Value("${PrometheusClient.host}") String host,
                                  @Value("${PrometheusClient.pushGatewayHost}") String pushGatewayHost){
        this.prometheusHost = host;
        this.pushGatewayHost = pushGatewayHost;
    }

    @Bean(name = "PrometheusClientXHttpClient")
    @Scope("prototype")
    public Client xhttpClient(){
        return XHttpClient.create(prometheusHost);
    }

    @Bean(name = "PrometheusPushGateway")
    @Scope("prototype")
    public PushGateway getPushGateway(){
        return new PushGateway(this.pushGatewayHost);
    }
}
