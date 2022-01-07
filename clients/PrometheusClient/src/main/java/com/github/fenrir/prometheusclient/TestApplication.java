package com.github.fenrir.prometheusclient;

import com.github.fenrir.prometheusclient.entities.PrometheusQueryResponse;
import com.github.fenrir.prometheusclient.entities.PrometheusQueryResult;
import com.github.fenrir.prometheusclient.services.PrometheusDataPushService;
import com.github.fenrir.prometheusclient.services.PrometheusDataService;
import io.prometheus.client.Counter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;

@SpringBootApplication(
        exclude = {
                MongoDataAutoConfiguration.class,
                MongoAutoConfiguration.class
        }
)
public class TestApplication {
    static public void main(String[] args) throws InterruptedException, IOException {
        ConfigurableApplicationContext context = SpringApplication.run(TestApplication.class);

        /*PrometheusDataService service = context.getBean("PrometheusDataServiceDefault", PrometheusDataService.class);
        service.init(context);
        PrometheusQueryResponse response = service.query("system_cpu_system_usage{hostname=\"node3\"," +
                        "instance=\"192.168.137.191:8089\",job=\"k8s-v1.22.4-xlocalmonitor-CpuUsageMonitor\"," +
                        "nodeId=\"localmonitor.a6504dffdffd10669a2dc1eff60f1e0a74dad6be0cb95a8758cfbc19002f3fa5\"}[3m]",
                null);
        System.out.println(response.resultsNum());
        for(PrometheusQueryResult result : response.getResults()){
            System.out.println(result);
        }*/


        PrometheusDataPushService pushService = context.getBean("PrometheusDataPushServiceDefault", PrometheusDataPushService.class);
        pushService.init(context);
        String counterLabel = pushService.registerCounterWithRegistry("xds_test_job", "xds_pushgateway_test", "for test", "label1");
        String gaugeLabel = pushService.registerGaugeWithRegistry("xds_test_job", "xds_gauge", "gauge for test");


        for(int i = 0; i < 100; i++){
            pushService.gaugeIncrease(gaugeLabel, 2.0);
            pushService.counterIncrease(counterLabel, 2.0, "1");
            pushService.counterIncrease(counterLabel, 2.0, "2");

            pushService.pushJob("xds_test_job");

            pushService.counterIncrease(counterLabel, 2.0, "3");
            Thread.sleep(2000);
        }
    }
}
