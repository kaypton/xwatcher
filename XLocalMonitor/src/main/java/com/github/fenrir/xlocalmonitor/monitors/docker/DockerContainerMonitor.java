package com.github.fenrir.xlocalmonitor.monitors.docker;

import com.github.fenrir.xcommon.utils.CommonUtils;
import com.github.fenrir.xlocalmonitor.annotations.Monitor;
import com.github.fenrir.xlocalmonitor.entities.MessageBuilder;
import com.github.fenrir.xlocalmonitor.inspectors.thirdpart.clients.dockerclient.DockerAPI;
import com.github.fenrir.xlocalmonitor.monitors.BaseMonitor;
import com.github.fenrir.xlocalmonitor.services.prometheus.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;

/**
 * @author DiShi Xu
 */
@Monitor(name = "DockerContainerMonitor",
        streams = {"docker.cpu.usage"},
        inspectors = {"docker"})
public class DockerContainerMonitor extends BaseMonitor {
    static private final Logger logger = LoggerFactory.getLogger("DockerContainerMonitor");

    static private final String DOCKER_CONTAINER_CPU_TOTAL_USAGE = "docker_container_cpu_total_usage";
    static private final String DOCKER_CONTAINER_MEMORY_USAGE = "docker_container_memory_usage";
    static private final String DOCKER_CONTAINER_NUM_PROCS = "docker_container_num_procs";

    private final DockerAPI dockerAPI;
    private Map<String, DockerAPI.DockerStatistics> lastMetric = new ConcurrentHashMap<>();
    private DataContainer dataContainer;

    private final Map<String, BlockingQueue<DockerAPI.DockerStatistics>> statisticsBlockingQueueMap;
    private final ThreadPoolExecutor pollExecutor = new ThreadPoolExecutor(
            3, 200, 200, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>()
    );

    private static class DockerContainerMonitorTimerTask extends TimerTask {

        private final DockerAPI dockerAPI;
        private final Map<String, BlockingQueue<DockerAPI.DockerStatistics>> statisticsBlockingQueueMap;
        private final DataContainer dataContainer;
        private final DockerContainerMonitor monitor;

        public DockerContainerMonitorTimerTask(DockerAPI dockerAPI,
                                               Map<String, BlockingQueue<DockerAPI.DockerStatistics>> statisticsBlockingQueueMap,
                                               DataContainer dataContainer,
                                               DockerContainerMonitor monitor){
            this.dockerAPI = dockerAPI;
            this.statisticsBlockingQueueMap = statisticsBlockingQueueMap;
            this.dataContainer = dataContainer;
            this.monitor = monitor;
        }

        @Override
        public void run() {
            Map<String, String[]> containerIdNames = this.dockerAPI.getUpContainerIdNames();
            for(String containerId : containerIdNames.keySet()){
                if(this.statisticsBlockingQueueMap.containsKey(containerId)){
                    this.dockerAPI.startStats(containerId, this.statisticsBlockingQueueMap.get(containerId));
                }else{
                    BlockingQueue<DockerAPI.DockerStatistics> queue = new LinkedBlockingQueue<>();
                    this.dockerAPI.startStats(containerId, queue);
                    this.statisticsBlockingQueueMap.put(containerId, queue);

                    Data data = new Data(DOCKER_CONTAINER_CPU_TOTAL_USAGE,
                            this.monitor.getDataMethod(),
                            "docker container cpu total usage",
                            MetricType.GAUGE,
                            DOCKER_CONTAINER_CPU_TOTAL_USAGE + "_" + containerId,
                            containerId);

                    data.addLabel("nodeId", this.monitor.getUuid());
                    data.addLabel("hostname", this.monitor.getHostname());
                    data.addLabel("containerId", containerId);
                    int index = 1;
                    for(String name : containerIdNames.get(containerId)){
                        data.addLabel("containerName" + index, name);
                        index++;
                    }

                    dataContainer.registerData(data);

                    data = new Data(DOCKER_CONTAINER_MEMORY_USAGE,
                            this.monitor.getDataMethod(),
                            "docker container memory total usage",
                            MetricType.GAUGE,
                            DOCKER_CONTAINER_MEMORY_USAGE + "_" + containerId,
                            containerId);

                    data.addLabel("nodeId", this.monitor.getUuid());
                    data.addLabel("hostname", this.monitor.getHostname());
                    data.addLabel("containerId", containerId);

                    index = 1;
                    for(String name : containerIdNames.get(containerId)){
                        data.addLabel("containerName" + index, name);
                        index++;
                    }

                    dataContainer.registerData(data);

                    data = new Data(DOCKER_CONTAINER_NUM_PROCS,
                            this.monitor.getDataMethod(),
                            "docker container num procs",
                            MetricType.GAUGE,
                            DOCKER_CONTAINER_NUM_PROCS + "_" + containerId,
                            containerId);

                    data.addLabel("nodeId", this.monitor.getUuid());
                    data.addLabel("hostname", this.monitor.getHostname());
                    data.addLabel("containerId", containerId);

                    index = 1;
                    for(String name : containerIdNames.get(containerId)){
                        data.addLabel("containerName" + index, name);
                        index++;
                    }

                    dataContainer.registerData(data);

                }
            }

            for(String containerId : this.statisticsBlockingQueueMap.keySet()){
                if(!containerIdNames.containsKey(containerId)){
                    this.dockerAPI.stopStats(containerId);
                    this.statisticsBlockingQueueMap.remove(containerId);

                    dataContainer.unregisterData(DOCKER_CONTAINER_CPU_TOTAL_USAGE + "_" + containerId);
                    dataContainer.unregisterData(DOCKER_CONTAINER_MEMORY_USAGE + "_" + containerId);
                    dataContainer.unregisterData(DOCKER_CONTAINER_NUM_PROCS + "_" + containerId);
                }
            }
        }
    }

    public void setLastMetric(String containerId, DockerAPI.DockerStatistics lastMetric) {
        this.lastMetric.put(containerId, lastMetric);
    }

    public DockerAPI.DockerStatistics getLastMetric(String containerId){
        return this.lastMetric.getOrDefault(containerId, null);
    }

    public DockerContainerMonitor() {
        this.dockerAPI = ((DockerAPI) this.getApiMap().get("docker"));
        this.statisticsBlockingQueueMap = new ConcurrentHashMap<>();
    }

    @Override
    protected void postStart() {

    }

    @Override
    protected void doStart() {
        this.registerTimerTask(new DockerContainerMonitorTimerTask(
                this.dockerAPI,
                this.statisticsBlockingQueueMap,
                this.dataContainer,
                this
        ), (long) 1000);

        while(true){
            Map<String, Double> cpuUsageData = new HashMap<>();
            Map<String, Future<DockerAPI.DockerStatistics>> futures = new ConcurrentHashMap<>();
            for(String containerId : this.statisticsBlockingQueueMap.keySet()){
                if(this.statisticsBlockingQueueMap.containsKey(containerId)){
                    BlockingQueue<DockerAPI.DockerStatistics> queue =
                            this.statisticsBlockingQueueMap.get(containerId);
                    Callable<DockerAPI.DockerStatistics> pollCallableTask = () -> queue.poll(1, TimeUnit.SECONDS);
                    Future<DockerAPI.DockerStatistics> futureStats =
                            this.pollExecutor.submit(pollCallableTask);
                    futures.put(containerId, futureStats);
                }
            }

            for(String containerId : futures.keySet()){
                try {
                    DockerAPI.DockerStatistics stats = futures.get(containerId).get(1, TimeUnit.SECONDS);
                    if(stats != null) {
                        this.setLastMetric(containerId, stats);
                        cpuUsageData.put(containerId, stats.getTotalCpuUsage());
                    }
                } catch (InterruptedException | ExecutionException | TimeoutException ignored) {
                }
            }

            try{
                this.sendStreamData("docker.cpu.usage",
                        MessageBuilder.builder("Stream", "docker.cpu.usage")
                                .withValue("total.cpu.usage", cpuUsageData, Map.class)
                                .withValue("timestamp", CommonUtils.getTimestamp(), Long.class)
                                .withObject("hostInfo") /* HostInfo */
                                .withValue("host", this.getHostname(), String.class)
                                .withValue("monitorId", this.getUuid(), String.class)
                                .buildObject()          /* end HostInfo */
                                .build()
                );
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void preStart() {
        logger.info("monitor start running ...");

        this.dataContainer = new DataContainer("DockerContainerMonitor");
        DataContainerService.registerDataContainer(dataContainer);
    }

    @Override
    public void doStop() {

    }

    @Override
    public Map<String, Map<String, Object>> extract() {
        return null;
    }

    public GetDataMethod getDataMethod(){
        return (metricName, params) -> {
            DockerAPI.DockerStatistics stats = this.getLastMetric(params[0]);
            switch (metricName) {
                case DOCKER_CONTAINER_CPU_TOTAL_USAGE:
                    if(stats != null)
                        return stats.getTotalCpuUsage();
                    break;
                case DOCKER_CONTAINER_MEMORY_USAGE:
                    if(stats != null)
                        return (double) stats.getMemoryUsage();
                    break;
                case DOCKER_CONTAINER_NUM_PROCS:
                    if(stats != null)
                        return (double) stats.getNumProcs();
            }
            return null;
        };
    }
}
