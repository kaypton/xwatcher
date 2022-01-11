package com.github.fenrir.xservicedependency.configs;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.InfluxDBClientOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InfluxDBConfig {

    private final String influxdbUrl;

    private final String username;
    private final String password;
    private final String token;
    private final String bucket;
    private final String org;

    public InfluxDBConfig(@Value("${XServiceDependency.influxdb.influxdbUrl}") String influxdbUrl,
                          @Value("${XServiceDependency.influxdb.detail.username}") String username,
                          @Value("${XServiceDependency.influxdb.detail.password}") String password,
                          @Value("${XServiceDependency.influxdb.detail.org}") String org,
                          @Value("${XServiceDependency.influxdb.detail.token}") String token,
                          @Value("${XServiceDependency.influxdb.detail.bucket}") String bucket){
        this.influxdbUrl = influxdbUrl;

        this.username = username;
        this.password = password;
        this.token = token;
        this.bucket = bucket;
        this.org = org;
    }

    @Bean(name = "influxDBClient")
    public InfluxDBClient getInfluxDBClient(){
        InfluxDBClientOptions options = InfluxDBClientOptions.builder()
                .url(this.influxdbUrl)
                .authenticateToken(this.token.toCharArray())
                .org(this.org)
                .bucket(this.bucket)
                .authenticate(this.username, this.password.toCharArray())
                .build();
        return InfluxDBClientFactory.create(options);
    }
}
