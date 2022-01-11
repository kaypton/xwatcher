package com.github.fenrir.xtraceanomalydetector;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication(
        exclude = {
                MongoAutoConfiguration.class,
                MongoDataAutoConfiguration.class
        }
)
public class XTraceAnomalyDetectorApplication {

    static public ConfigurableApplicationContext context;

    static public void main(String[] args){
        context = SpringApplication.run(XTraceAnomalyDetectorApplication.class);
    }
}
