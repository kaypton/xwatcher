package com.github.fenrir.xlocalmonitor.inspectors.thirdpart.clients.dockerclient;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.CpuUsageConfig;
import com.github.dockerjava.api.model.StatisticNetworksConfig;
import com.github.dockerjava.api.model.Statistics;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import com.github.fenrir.xlocalmonitor.annotations.Inspector;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author DiShi Xu
 */
@Inspector(name = "docker")
public class DockerAPI {
    static private final Logger logger = LoggerFactory.getLogger("DockerAPI");

    public static class DockerNetworkStatistics {
        private final Long rxBytes;
        private final Long rxDropped;
        private final Long rxPackets;
        private final Long rxErrors;

        private final Long txBytes;
        private final Long txDropped;
        private final Long txPackets;
        private final Long txErrors;
        public DockerNetworkStatistics(Long rxBytes,
                                       Long rxDropped,
                                       Long rxPackets,
                                       Long rxErrors,
                                       Long txBytes,
                                       Long txDropped,
                                       Long txPackets,
                                       Long txErrors){
            this.rxBytes = rxBytes;
            this.rxPackets = rxPackets;
            this.rxDropped = rxDropped;
            this.rxErrors = rxErrors;
            this.txBytes = txBytes;
            this.txPackets = txPackets;
            this.txDropped = txDropped;
            this.txErrors = txErrors;
        }

        public Long getRxBytes() {
            return rxBytes;
        }

        public Long getRxDropped() {
            return rxDropped;
        }

        public Long getRxPackets() {
            return rxPackets;
        }

        public Long getRxErrors() {
            return rxErrors;
        }

        public Long getTxBytes() {
            return txBytes;
        }

        public Long getTxDropped() {
            return txDropped;
        }

        public Long getTxPackets() {
            return txPackets;
        }

        public Long getTxErrors() {
            return txErrors;
        }
    }

    public static class DockerStatistics {

        private final Double totalCpuUsage;

        private final Long memoryUsage;
        private final Long memoryMaxUsage;
        private final Long memoryLimit;
        private final Long memoryFailcnt;

        private final Long numProcs;
        private final Map<String, DockerNetworkStatistics> networks;

        public DockerStatistics(Double totalCpuUsage,
                                Long memoryUsage,
                                Long memoryMaxUsage,
                                Long memoryLimit,
                                Long memoryFailcnt,
                                Long numProcs,
                                Map<String, DockerNetworkStatistics> networks){
            this.totalCpuUsage = totalCpuUsage;

            this.memoryUsage = memoryUsage;
            this.memoryMaxUsage = memoryMaxUsage;
            this.memoryLimit = memoryLimit;
            this.memoryFailcnt = memoryFailcnt;

            this.numProcs = numProcs;
            this.networks = networks;
        }

        public Double getTotalCpuUsage() {
            return totalCpuUsage;
        }

        public Long getMemoryUsage() {
            return memoryUsage;
        }

        public Long getMemoryMaxUsage() {
            return memoryMaxUsage;
        }

        public Long getMemoryLimit() {
            return memoryLimit;
        }

        public Long getMemoryFailcnt() {
            return memoryFailcnt;
        }

        public Long getNumProcs() {
            return numProcs;
        }

        public Map<String, DockerNetworkStatistics> getNetworks() {
            return networks;
        }
    }

    @Getter @Setter private static String host = null;

    private final DockerClient dockerClient;

    private final Map<String, StatsResultCallback> statsResultCallbackMap;

    private static class StatsResultCallback implements ResultCallback<Statistics> {

        private final BlockingQueue<DockerStatistics> queue;
        private Closeable stream;
        public boolean isClosed = false;

        public StatsResultCallback(BlockingQueue<DockerStatistics> queue){
            this.queue = queue;
        }

        @Override
        public void onStart(Closeable closeable) {
            this.stream = closeable;
        }

        @Override
        public void onNext(Statistics object) {
            CpuUsageConfig cpuUsageConfig = object.getCpuStats().getCpuUsage();
            CpuUsageConfig preCpuUsageConfig = object.getPreCpuStats().getCpuUsage();

            Map<String, StatisticNetworksConfig> networksConfig = object.getNetworks();
            Map<String, DockerNetworkStatistics> networkStats = null;

            if(networksConfig != null){
                networkStats = new ConcurrentHashMap<>();
                for(String str : networksConfig.keySet()){
                    StatisticNetworksConfig tmp = networksConfig.get(str);
                    DockerNetworkStatistics nStats = new DockerNetworkStatistics(
                            tmp.getRxBytes(),
                            tmp.getRxDropped(),
                            tmp.getRxPackets(),
                            tmp.getRxErrors(),
                            tmp.getTxBytes(),
                            tmp.getTxDropped(),
                            tmp.getTxPackets(),
                            tmp.getTxErrors()
                    );
                    networkStats.put(str, nStats);
                }
            }

            Long containerTotalCpuUsage = null;
            Long systemTotalCpuUsage = null;
            Double containerTotalCpuUsagePercentage = null;

            if(cpuUsageConfig != null && preCpuUsageConfig != null){
                Long cpuTotalUsage = cpuUsageConfig.getTotalUsage();
                Long preCpuTotalUsage = preCpuUsageConfig.getTotalUsage();

                Long systemCpuUsage = object.getCpuStats().getSystemCpuUsage();
                Long preSystemCpuUsage = object.getPreCpuStats().getSystemCpuUsage();

                if(cpuTotalUsage != null && preCpuTotalUsage != null &&
                    systemCpuUsage != null && preSystemCpuUsage != null){
                    containerTotalCpuUsage = cpuTotalUsage - preCpuTotalUsage;
                    systemTotalCpuUsage = systemCpuUsage - preSystemCpuUsage;
                }
            }

            if(containerTotalCpuUsage != null){
                containerTotalCpuUsagePercentage = ((double) containerTotalCpuUsage) / ((double) systemTotalCpuUsage);
            }

            if(containerTotalCpuUsagePercentage != null){
                try {
                    this.queue.put(new DockerStatistics(
                            containerTotalCpuUsagePercentage,
                            object.getMemoryStats().getUsage(),
                            object.getMemoryStats().getMaxUsage(),
                            object.getMemoryStats().getLimit(),
                            object.getMemoryStats().getFailcnt(),
                            object.getNumProcs(),
                            networkStats
                    ));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onError(Throwable throwable) {

        }

        @Override
        public void onComplete() {
            try {
                this.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void close() throws IOException {
            stream.close();
            this.isClosed = true;
        }
    }

    public DockerAPI(){
        logger.info("Initialize");
        logger.info("docker daemon host: " + host);
        DockerClientConfig dockerClientConfig = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost(host)
                .withDockerTlsVerify(false)
                .build();

        DockerHttpClient dockerHttpClient = new ApacheDockerHttpClient.Builder()
                .dockerHost(dockerClientConfig.getDockerHost())
                .sslConfig(dockerClientConfig.getSSLConfig())
                .maxConnections(100)
                .connectionTimeout(Duration.ofSeconds(60))
                .responseTimeout(Duration.ofSeconds(45))
                .build();

        this.dockerClient = DockerClientBuilder.getInstance(dockerClientConfig)
                .withDockerHttpClient(dockerHttpClient)
                .build();
        logger.info("docker client init done");

        logger.info("get all containers");
        List<Container> containerList = this.dockerClient.listContainersCmd()
                .withShowAll(true)
                .withShowSize(true)
                .exec();
        logger.info("there are " + containerList.size() + " containers");
        for(Container container : containerList){
            logger.info("    container: " + container.getId() + " status:" + container.getStatus());
        }

        this.statsResultCallbackMap = new ConcurrentHashMap<>();
    }

    public void startStats(String containerId, BlockingQueue<DockerStatistics> queue){
        if(this.statsResultCallbackMap.containsKey(containerId)){
            if(this.statsResultCallbackMap.get(containerId).isClosed){
                this.statsResultCallbackMap.remove(containerId);
            }else{
                return;
            }
        }
        StatsResultCallback statsResultCallback = new StatsResultCallback(queue);
        this.statsResultCallbackMap.put(containerId, statsResultCallback);
        this.dockerClient.statsCmd(containerId).exec(statsResultCallback);
    }

    public void stopStats(String containerId){
        if(this.statsResultCallbackMap.containsKey(containerId)){
            try {
                this.statsResultCallbackMap.get(containerId).close();
                this.statsResultCallbackMap.remove(containerId);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public Map<String, String[]> getUpContainerIdNames(){
        Map<String, String[]> idNameMap = new HashMap<>();
        List<Container> containerList = getContainers();
        if(containerList != null){
            for(Container container : containerList){
                if(container.getStatus().startsWith("Up"))
                    idNameMap.put(container.getId(), container.getNames());
            }
        }
        return idNameMap;
    }

    public List<Container> getContainers(){
        try{
            return this.dockerClient.listContainersCmd()
                    .withShowAll(true)
                    .withShowSize(true)
                    .exec();
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
}
