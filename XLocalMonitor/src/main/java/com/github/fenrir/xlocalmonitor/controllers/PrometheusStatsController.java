package com.github.fenrir.xlocalmonitor.controllers;

import com.github.fenrir.xlocalmonitor.services.prometheus.DataContainerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/v1/stats/prometheus")
public class PrometheusStatsController {
    private DataContainerService dataContainerService;
    public PrometheusStatsController(DataContainerService dataContainerService){
        this.dataContainerService = dataContainerService;
    }

    @GetMapping(path = "/CpuUsageMonitor", produces = "text/plain")
    public String cpuUsageMonitor(){
        return this.dataContainerService.getMetricPlainTextString("CpuUsageMonitor");
    }

    @GetMapping(path = "/MemoryMonitor", produces = "text/plain")
    public String memoryMonitor(){
        return this.dataContainerService.getMetricPlainTextString("MemoryMonitor");
    }

    @GetMapping(path = "/DockerContainerMonitor", produces = "text/plain")
    public String dockerContainerMonitor() {
        return this.dataContainerService.getMetricPlainTextString("DockerContainerMonitor");
    }
}
