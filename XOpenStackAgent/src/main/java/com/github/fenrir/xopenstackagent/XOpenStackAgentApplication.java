package com.github.fenrir.xopenstackagent;

import com.github.fenrir.xopenstackagent.configs.ArgsConfig;
import com.github.fenrir.xopenstackagent.services.OpenStackAgentService;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class XOpenStackAgentApplication {
    public static void main(String[] args){
        ArgsConfig argsConfig = new ArgsConfig();
        CmdLineParser cmdLineParser = new CmdLineParser(argsConfig);
        try{
            cmdLineParser.parseArgument(args);
        } catch (CmdLineException e){
            e.printStackTrace();
            return;
        }

        if(argsConfig.getKeystoneAddress() == null ||
           argsConfig.getOpenstackDomainName() == null ||
           argsConfig.getOpenstackUsername() == null ||
           argsConfig.getOpenstackPassword() == null ||
           argsConfig.getOpenstackProjectUUID() == null){
            cmdLineParser.printUsage(System.out);
            return;
        }

        OpenStackAgentService.setKeystoneAddress(argsConfig.getKeystoneAddress());
        OpenStackAgentService.setUsername(argsConfig.getOpenstackUsername());
        OpenStackAgentService.setDomainName(argsConfig.getOpenstackDomainName());
        OpenStackAgentService.setPassword(argsConfig.getOpenstackPassword());
        OpenStackAgentService.setProjectUUID(argsConfig.getOpenstackProjectUUID());

        SpringApplication.run(XOpenStackAgentApplication.class);
    }
}
