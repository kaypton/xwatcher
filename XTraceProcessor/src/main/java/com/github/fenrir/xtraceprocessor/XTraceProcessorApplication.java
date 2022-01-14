package com.github.fenrir.xtraceprocessor;

import com.github.fenrir.xtraceprocessor.services.CollectorService;
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
                "com.github.fenrir.xtraceprocessor",
                "com.github.fenrir.prometheusclient"
        }
)
public class XTraceProcessorApplication {
    static public ConfigurableApplicationContext context;

    static public void main(String[] args){
        context = SpringApplication.run(XTraceProcessorApplication.class);
        context.getBean(CollectorService.class).startup();
    }
}
