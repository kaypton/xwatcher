package com.github.fenrir.xstrategy.services.strategies.antcolonysystemservice;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.fenrir.xcommon.actions.Action;
import com.github.fenrir.xcommon.actions.containers.ContainerMigrateAction;
import com.github.fenrir.xcommon.clients.BaseResponse;
import com.github.fenrir.xcommon.clients.xstrategy.api.rest.entities.AntColonySystemResultResponse;
import com.github.fenrir.xcommon.clients.xstrategy.api.rest.entities.AntColonySystemTestResponse;
import com.github.fenrir.xcommon.clients.xstrategy.api.rest.entities.AntColonySystemTriggerResponse;
import com.github.fenrir.xmessaging.*;
import com.github.fenrir.xstrategy.services.planservice.PlanService;
import com.github.fenrir.xstrategy.services.strategies.StrategyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service("antColonySystem")
public class AntColonySystemService implements StrategyService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AntColonySystemService.class);

    private final XMessaging messaging;
    private XMessagingListener hostCpuUsageStreamListener;
    private XMessagingListener dockerCpuUsageStreamListener;

    private final Lock copyLock = new ReentrantLock();
    private final Map<String, AntColonySystem.Host> hostMap = new ConcurrentHashMap<>();
    private final Map<String, AntColonySystem.Parasite> parasiteMap = new ConcurrentHashMap<>();

    private PlanService planService;

    private static class MessagingProcessor implements MessageProcessCallBack {

        enum Type {
            HOST,
            PARASITE
        }

        private final Map<String, AntColonySystem.Host> hostMap;
        private final Map<String, AntColonySystem.Parasite> parasiteMap;
        private final Lock copyLock;
        private final Type type;

        public MessagingProcessor(Map<String, AntColonySystem.Host> hostMap,
                                  Map<String, AntColonySystem.Parasite> parasiteMap,
                                  Lock copyLock,
                                  Type type){
            this.hostMap = hostMap;
            this.parasiteMap = parasiteMap;
            this.copyLock = copyLock;
            this.type = type;
        }

        @Override
        public void processMessage(XMessage msg) {
            doProcessMessage(msg.getStringData());
        }

        private void doProcessMessage(String json){

            JSONObject metricJSON = JSON.parseObject(json);
            if(this.type == Type.HOST){
                String hostname = metricJSON.getJSONObject("hostInfo").getString("host");
                Integer coreNum = metricJSON.getJSONObject("value").getInteger("cpu.core.num");
                Double cpuTotalUsage = metricJSON.getJSONObject("value").getDouble("cpu.total.usage");
                if(this.hostMap.containsKey(hostname)){
                    this.hostMap.get(hostname).capacity = (double) coreNum * 1000;
                    this.hostMap.get(hostname).used = cpuTotalUsage * ((double) coreNum * 1000);
                }else{
                    AntColonySystem.Host host = new AntColonySystem.Host(
                            (double) coreNum * 1000,
                            cpuTotalUsage * ((double) coreNum * 1000),
                            0
                    );
                    this.hostMap.put(hostname, host);
                }
            }else if(this.type == Type.PARASITE){
                String hostname = metricJSON.getJSONObject("hostInfo").getString("host");
                if(!this.hostMap.containsKey(hostname)) return;
                JSONObject totalCpuUsageMap = metricJSON.getJSONObject("total.cpu.usage");
                for(String parasiteName : totalCpuUsageMap.keySet()){
                    if(this.parasiteMap.containsKey(parasiteName)){
                        this.parasiteMap.get(parasiteName).resourceNeeded = totalCpuUsageMap.getDoubleValue(parasiteName) *
                                this.hostMap.get(hostname).capacity;
                        this.parasiteMap.get(parasiteName).originHost = hostname;
                    }else{
                        AntColonySystem.Parasite parasite = new AntColonySystem.Parasite(
                                totalCpuUsageMap.getDoubleValue(parasiteName) *
                                this.hostMap.get(hostname).capacity,
                                hostname
                        );
                        this.parasiteMap.put(parasiteName, parasite);
                    }
                }
            }
        }
    }

    public AntColonySystemService(@Autowired XMessagingConfiguration xMessagingConfiguration,
                                  @Autowired PlanService planService){
        this.planService = planService;
        this.messaging = XMessaging.create(xMessagingConfiguration);
    }

    private final ThreadPoolExecutor executor = new ThreadPoolExecutor(
            1, 1, 0, TimeUnit.SECONDS, new LinkedBlockingQueue<>()
    );
    private AntColonySystem currentSystem = null;

    @Override
    public AntColonySystemTriggerResponse trigger(){
        LOGGER.info("Ant Colony System Strategy running ...");
        AntColonySystemTriggerResponse response = new AntColonySystemTriggerResponse();

        if(this.executor.getActiveCount() > 0){
            response.status = 1;
            response.msg = "Still running";
            return response;
        }

        Map<String, AntColonySystem.Host> hostMap = this.copyHosts();
        if(hostMap == null){
            response.status = 2;
            response.msg = "Resource does not sync yet";
            return response;
        }
        Map<String, AntColonySystem.Parasite> parasiteMap = this.copyParasites();

        Map<String, AntColonySystem.Host> underloadHost = new ConcurrentHashMap<>();
        Map<String, AntColonySystem.Host> overloadHost = new ConcurrentHashMap<>();

        for(String hostname : hostMap.keySet()){
            AntColonySystem.Host h = hostMap.get(hostname);
            if(h.used / h.capacity >= 0.5){
                overloadHost.put(hostname, h);
            }else{
                underloadHost.put(hostname, h);
            }
        }

        AntColonySystem system = new AntColonySystem(
                150,
                1000,
                0.8,
                1,
                2,
                underloadHost,
                overloadHost,
                parasiteMap
        );

        system.setIterNum(25);
        this.executor.execute(system);
        this.currentSystem = system;

        response.status = 0;
        response.msg = "Start running";
        return response;
    }

    private Map<String, AntColonySystem.Host> copyHosts(){
        Map<String, AntColonySystem.Host> _hostMap = new ConcurrentHashMap<>();
        for(String hostname : this.hostMap.keySet()){
            AntColonySystem.Host toBeCopy = this.hostMap.get(hostname);
            AntColonySystem.Host h = new AntColonySystem.Host(
                    toBeCopy.used,
                    toBeCopy.capacity,
                    toBeCopy.parasiteNum
            );
            _hostMap.put(hostname, h);
        }
        for(String parasiteName : this.parasiteMap.keySet()){
            String originHostName = this.parasiteMap.get(parasiteName).originHost;
            if(!_hostMap.containsKey(originHostName))
                return null;
            _hostMap.get(originHostName).parasiteNum += 1;
        }
        return _hostMap;
    }

    private Map<String, AntColonySystem.Parasite> copyParasites(){
        Map<String, AntColonySystem.Parasite> _parasiteMap = new ConcurrentHashMap<>();
        for(String parasiteName : this.parasiteMap.keySet()){
            AntColonySystem.Parasite toByCopy = this.parasiteMap.get(parasiteName);
            AntColonySystem.Parasite p = new AntColonySystem.Parasite(
                    toByCopy.resourceNeeded,
                    toByCopy.originHost
            );
            _parasiteMap.put(parasiteName, p);
        }
        return _parasiteMap;
    }

    @Override
    public void startup(){
        this.hostCpuUsageStreamListener = this.messaging.getListener("stream.system.cpu.usage",
                new MessagingProcessor(this.hostMap, this.parasiteMap, this.copyLock, MessagingProcessor.Type.HOST));
        this.dockerCpuUsageStreamListener = this.messaging.getListener("stream.docker.cpu.usage",
                new MessagingProcessor(this.hostMap, this.parasiteMap, this.copyLock, MessagingProcessor.Type.PARASITE));
    }

    // 蚁群算法最后的结果由 result 方法来触发
    @Override
    public BaseResponse result(){
        AntColonySystemResultResponse response = new AntColonySystemResultResponse();
        if(this.executor.getActiveCount() > 0){
            response.status = 1;
            response.msg = "Still running";
            response.migratePlan = null;
        }else{
            response.status = 0;
            response.msg = "Success";
            response.migratePlan = this.currentSystem.getBestMigrateMap();
        }

        doAntColonySystemService(this.currentSystem.getBestMigrateMap());

        return response;
    }

    @Override
    public BaseResponse test(){
        AntColonySystemTestResponse<AntColonySystem.Host, AntColonySystem.Parasite> response =
                new AntColonySystemTestResponse<>();
        response.hosts = this.hostMap;
        response.parasite = this.parasiteMap;
        response.status = 0;
        response.msg = "Success";
        return response;
    }

    private void doAntColonySystemService(Map<String, String> migrateMap){
        String planUUID = this.planService.createNewPlan("kubernetes");

        for(String containerId : migrateMap.keySet()){
            ContainerMigrateAction.OptsData opts = new ContainerMigrateAction.OptsData();
            opts.setContainerId(containerId);
            opts.setDestHostname(migrateMap.get(containerId));
            Action<ContainerMigrateAction.OptsData> action = Action.createAction(
                    opts,
                    ContainerMigrateAction.class,
                    ContainerMigrateAction.OptsData.class
            );

            LOGGER.info("create action: {}", JSON.toJSONString(action));
            this.planService.addActionToPlan(planUUID, action);
        }

        this.planService.executePlan(planUUID);
    }

    /*static public void main(String[] args){
        ContainerMigrateAction.OptsData opts = new ContainerMigrateAction.OptsData();
        opts.setDestHostname("node1");
        opts.setContainerId("123");
        Action<ContainerMigrateAction.OptsData> action = Action.createAction(
                opts,
                ContainerMigrateAction.class,
                ContainerMigrateAction.OptsData.class
        );
        System.out.println(action.toJSONString());
    }*/
}
