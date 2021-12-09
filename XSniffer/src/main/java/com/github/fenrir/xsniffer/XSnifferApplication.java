package com.github.fenrir.xsniffer;

import com.alibaba.fastjson.JSONObject;
import com.github.fenrir.xmessaging.XMessaging;
import com.github.fenrir.xsniffer.configs.ArgsConfig;
import com.github.fenrir.xsniffer.exporter.Exporter;
import com.github.fenrir.xsniffer.exporter.ExporterFactory;
import com.github.fenrir.xsniffer.listener.EventListener;
import com.github.fenrir.xsniffer.listener.StreamListener;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class XSnifferApplication {
    private static final Logger logger = LoggerFactory
            .getLogger("XSnifferApplication");

    private static final ThreadPoolExecutor executor = new ThreadPoolExecutor(
            1, 1, 0, TimeUnit.SECONDS, new LinkedBlockingQueue<>());

    public static void main(String[] args){
        // parse cmdline args
        ArgsConfig argsConfig = new ArgsConfig();
        CmdLineParser cmdLineParser = new CmdLineParser(argsConfig);
        try {
            cmdLineParser.parseArgument(args);
        } catch (CmdLineException e) {
            e.printStackTrace();
            return;
        }

        logger.info("[CONFIG] nats servers : " + argsConfig.getNatsServerAddresses());
        logger.info("[CONFIG] XRegistry host : " + argsConfig.getXRegistryAddress());

        if(argsConfig.getXRegistryAddress() == null ||
                argsConfig.getNatsServerAddresses() == null){
            cmdLineParser.printUsage(System.out);
            return;
        }

        // init XMessaging
        XMessaging.init(argsConfig.getNatsServerAddresses());

        if(argsConfig.getListenToEvent() != null && argsConfig.getListenToStream() != null){
            logger.error("just listen to event or stream, can't listen both");
            System.exit(-1);
        }

        if(argsConfig.getListenToEvent() == null && argsConfig.getListenToStream() == null){
            logger.error("you want me listen to what?");
            System.exit(-1);
        }

        Exporter exporter = null;
        if(argsConfig.getOutputFile() != null){
            Map<String, Object> extraParam = new HashMap<>();
            extraParam.put("filename", argsConfig.getOutputFile());
            if(argsConfig.getOutputFormat().equals("json"))
                exporter = ExporterFactory.create(ExporterFactory.Output.FILE,
                        ExporterFactory.OutputFormat.JSON, extraParam);
            else if(argsConfig.getOutputFormat().equals("csv"))
                exporter = ExporterFactory.create(ExporterFactory.Output.FILE,
                        ExporterFactory.OutputFormat.CSV, extraParam);
            else {
                logger.error("unknown output file format");
            }
        }

        logger.info("loading ... ");
        if(argsConfig.getListenToStream() != null){
            executor.execute(new StreamListener(argsConfig.getListenToStream(), exporter));
        }else{
            executor.execute(new EventListener(argsConfig.getListenToEvent(), exporter));
        }
        logger.info("executing ... ");
    }
}
