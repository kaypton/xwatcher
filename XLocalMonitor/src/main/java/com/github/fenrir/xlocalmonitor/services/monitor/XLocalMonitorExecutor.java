package com.github.fenrir.xlocalmonitor.services.monitor;

import com.github.fenrir.xlocalmonitor.configs.ApplicationConfig;
import com.github.fenrir.xlocalmonitor.monitors.BaseMonitor;
import com.github.fenrir.xlocalmonitor.services.pipeline.PipelineContainer;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Service
public class XLocalMonitorExecutor {
    private static final Logger logger = LoggerFactory.getLogger(
            "XLocalMonitorExecutor");

    @Getter private static final Integer maxMonitorNum = 10;

    @Getter private static final Map<String, Class<?>> executingMonitorMap =
            new HashMap<>();

    private static final ThreadPoolExecutor executor =
            new ThreadPoolExecutor(
                    maxMonitorNum,
                    maxMonitorNum,
                    0, TimeUnit.SECONDS,
                    new LinkedBlockingQueue<>());

    @Getter @Setter private PipelineContainer pipelineContainer = null;

    public XLocalMonitorExecutor(@Autowired PipelineContainer pipelineContainer){
        this.setPipelineContainer(pipelineContainer);
    }

    public static void startup(){
        if(ApplicationConfig.enableMonitors == null){
            logger.info("None monitor has been enabled");
        }else{
            for(String monitorName : ApplicationConfig.enableMonitors){
                BaseMonitor monitor = XLocalMonitorFactory.getMonitorInstanceFromName(monitorName);
                // monitor.setPipelineContainer(this.getPipelineContainer());
                execute(monitor);
            }
        }
    }

    public Integer getExecutingNum(){
        return executor.getActiveCount();
    }

    private static void execute(BaseMonitor monitor){
        executor.execute(monitor);
    }

    private void stop(){
        // TODO stop
    }
}
