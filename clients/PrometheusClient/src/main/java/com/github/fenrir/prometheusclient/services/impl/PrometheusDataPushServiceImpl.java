package com.github.fenrir.prometheusclient.services.impl;

import com.github.fenrir.prometheusclient.services.PrometheusDataPushService;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.prometheus.client.exporter.PushGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service("PrometheusDataPushServiceDefault")
public class PrometheusDataPushServiceImpl implements PrometheusDataPushService {
    private final static Logger LOGGER = LoggerFactory.getLogger(PrometheusDataPushServiceImpl.class);

    private ConfigurableApplicationContext context = null;

    private PushGateway pushGateway = null;

    // label collector map
    private Map<String, Counter> counterMap = new ConcurrentHashMap<>();
    // label collector map
    private Map<String, Gauge> gaugeMap = new ConcurrentHashMap<>();

    // job collector registry map
    private Map<String, CollectorRegistry> collectorRegistryMap = new ConcurrentHashMap<>();

    public void init(ConfigurableApplicationContext context){
        this.context = context;
    }

    private void _registryCounter(String label, String name, String help, String... labelNames){
        Counter counter = this.counterMap.getOrDefault(label, null);
        if(counter == null){
            counter = Counter.build().name(name).help(help).labelNames(labelNames).register();
            this.counterMap.put(label, counter);
        }
    }

    private void _registryGauge(String label, String name, String help, String... labelNames){
        Gauge gauge = this.gaugeMap.getOrDefault(label, null);
        if(gauge == null){
            gauge = Gauge.build().name(name).help(help).labelNames(labelNames).register();
            this.gaugeMap.put(label, gauge);
        }
    }

    @Override
    public String registerCounter(String name, String help, String... labelNames){
        String label = this.dataLabel(name, labelNames);
        this._registryCounter(label, name, help, labelNames);
        return label;
    }

    @Override
    public String registerGauge(String name, String help, String... labelNames){
        String label = this.dataLabel(name, labelNames);
        this._registryGauge(label, name, help, labelNames);
        return label;
    }

    @Override
    public String registerCounterWithRegistry(String jobName, String name, String help, String... labelNames) {
        String label = this.dataLabel(name, labelNames);
        if(this.counterMap.containsKey(label)){
            // already registered
            return label;
        }
        CollectorRegistry collectorRegistry = this.collectorRegistryMap.getOrDefault(jobName, null);
        if(collectorRegistry == null){
            collectorRegistry = new CollectorRegistry();
            this.collectorRegistryMap.put(jobName, collectorRegistry);
        }
        Counter counter = Counter.build().name(name).help(help).labelNames(labelNames).register(collectorRegistry);
        this.counterMap.put(label, counter);
        return label;
    }

    @Override
    public String registerGaugeWithRegistry(String jobName, String name, String help, String... labelNames) {
        String label = this.dataLabel(name, labelNames);
        if(this.gaugeMap.containsKey(label)){
            // already registered
            return label;
        }
        CollectorRegistry collectorRegistry = this.collectorRegistryMap.getOrDefault(jobName, null);
        if(collectorRegistry == null){
            collectorRegistry = new CollectorRegistry();
            this.collectorRegistryMap.put(jobName, collectorRegistry);
        }
        Gauge gauge = Gauge.build().name(name).help(help).labelNames(labelNames).register(collectorRegistry);
        this.gaugeMap.put(label, gauge);
        return label;
    }

    private String dataLabel(String name, String... labelNames){
        StringBuilder sb = new StringBuilder();
        sb.append(name);
        for(String labelName : labelNames){
            sb.append("_").append(labelName);
        }
        return sb.toString();
    }

    private PushGateway getPushGateway(){
        if(this.pushGateway == null){
            if(this.context == null){
                LOGGER.error("Spring Application Context is null, please init first");
                return null;
            }
            this.pushGateway = this.context.getBean("PrometheusPushGateway", PushGateway.class);
        }
        return pushGateway;
    }

    @Override
    public void counterIncrease(String label, Double value, String... labels){
        Counter counter = this.counterMap.getOrDefault(label, null);
        if(counter == null){
            LOGGER.error("counter do not exist");
            return;
        }
        counter.labels(labels).incWithExemplar();
        if(value == null){
            counter.labels(labels).inc();
        }else{
            counter.labels(labels).inc(value);
        }
    }

    @Override
    public void gaugeIncrease(String label, Double value, String... labels) {
        Gauge gauge = this.gaugeMap.getOrDefault(label, null);
        if(gauge == null){
            LOGGER.error("gauge do not exist");
            return;
        }
        if(value == null){
            gauge.labels(labels).inc();
        }else{
            gauge.labels(labels).inc(value);
        }
    }

    @Override
    public void gaugeDecrease(String label, Double value, String... labels) {
        Gauge gauge = this.gaugeMap.getOrDefault(label, null);
        if(gauge == null){
            LOGGER.error("gauge do not exist");
            return;
        }
        if(value == null){
            gauge.labels(labels).dec();
        }else{
            gauge.labels(labels).dec(value);
        }
    }

    @Override
    public void gaugeSet(String label, @NotNull Double value, String... labels) {
        Gauge gauge = this.gaugeMap.getOrDefault(label, null);
        if(gauge == null){
            LOGGER.error("gauge do not exist");
            return;
        }
        gauge.labels(labels).set(value);
    }

    @Override
    public void pushCounter(String label, String job) throws IOException {
        if(this.collectorRegistryMap.containsKey(job)){
            LOGGER.error("there are collectorRegistry with the same job {}", job);
            return;
        }
        Counter counter = this.counterMap.getOrDefault(label, null);
        if(counter == null){
            LOGGER.error("counter do not exist");
            return;
        }
        PushGateway pg = this.getPushGateway();
        if(pg != null)
            pg.push(counter, job);
    }

    @Override
    public void pushGauge(String label, String job) throws IOException {
        if(this.collectorRegistryMap.containsKey(job)){
            LOGGER.error("there are collectorRegistry with the same job {}", job);
            return;
        }
        Gauge gauge = this.gaugeMap.getOrDefault(label, null);
        if(gauge == null){
            LOGGER.error("counter do not exist");
            return;
        }
        PushGateway pg = this.getPushGateway();
        if(pg != null)
            pg.push(gauge, job);
    }

    public void pushJob(String jobName) throws IOException {
        PushGateway pg = this.getPushGateway();
        if(pg != null){
            if(!this.collectorRegistryMap.containsKey(jobName)){
                LOGGER.error("there is no collectorRegistry with job name {}", jobName);
                return;
            }
            pg.push(this.collectorRegistryMap.get(jobName), jobName);
        }
    }

}
