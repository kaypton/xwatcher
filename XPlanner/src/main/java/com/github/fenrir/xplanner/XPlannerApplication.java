package com.github.fenrir.xplanner;

import com.github.fenrir.xplanner.services.AgentService;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class XPlannerApplication {
    public static void main(String[] args){
        SpringApplication.run(XPlannerApplication.class);
    }
}
