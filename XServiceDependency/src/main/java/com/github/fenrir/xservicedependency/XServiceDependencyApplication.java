package com.github.fenrir.xservicedependency;

import com.github.fenrir.prometheusclient.services.PrometheusDataPushService;
import com.github.fenrir.xservicedependency.services.ReceiveService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication(
        exclude = {
                MongoAutoConfiguration.class,
                MongoDataAutoConfiguration.class
        },
        scanBasePackages = {
                "com.github.fenrir.xservicedependency",
                "com.github.fenrir.prometheusclient"
        }
)
public class XServiceDependencyApplication {
    static public ConfigurableApplicationContext context;

    static public void main(String[] args){
        context = SpringApplication.run(XServiceDependencyApplication.class);

        context.getBean("PrometheusDataPushServiceDefault", PrometheusDataPushService.class).init(context);
        context.getBean(ReceiveService.class).startup();
    }
}
