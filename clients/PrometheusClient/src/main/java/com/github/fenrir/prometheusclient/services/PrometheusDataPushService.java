package com.github.fenrir.prometheusclient.services;

import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;

public interface PrometheusDataPushService {
    void init(ConfigurableApplicationContext context);
    String registerCounter(String name, String help, String... labelNames);
    String registerGauge(String name, String help, String... labelNames);

    String registerCounterWithRegistry(String jobName, String name, String help, String... labelNames);
    String registerGaugeWithRegistry(String jobName, String name, String help, String... labelNames);

    void counterIncrease(String label, Double value, String... labels);
    void gaugeIncrease(String label, Double value, String... labels);
    void gaugeDecrease(String label, Double value, String... labels);
    void gaugeSet(String label, Double value, String... labels);

    void pushJob(String jobName) throws IOException;
    void pushCounter(String label, String job) throws IOException;
    void pushGauge(String label, String job) throws IOException;
}
