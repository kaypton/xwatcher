package com.github.fenrir.xdeepstrategy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;

@SpringBootApplication(
        exclude = { // exclude MongoDB auto configuration
                MongoDataAutoConfiguration.class,
                MongoAutoConfiguration.class
        }
)
public class XDeepStrategyApplication {
    public static void main(String[] args){
        SpringApplication.run(XDeepStrategyApplication.class);
    }
}
