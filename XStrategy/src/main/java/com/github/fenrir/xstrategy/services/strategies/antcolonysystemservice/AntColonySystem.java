package com.github.fenrir.xstrategy.services.strategies.antcolonysystemservice;

import com.github.fenrir.xcommon.utils.Tuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class AntColonySystem implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(AntColonySystem.class);

    public static class Host {  // MCore 毫核
        public double capacity;
        public double used;
        public int parasiteNum;
        public double tmp;

        public Host(double used, double capacity, int parasiteNum){
            this.capacity = capacity;
            this.used = used;
            this.parasiteNum = parasiteNum;
            this.tmp = this.used;
        }

        public String toString(){
            return "[capacity:" + capacity + ",used:" + used + ",parasite num:" + parasiteNum + "]";
        }
    }

    public static class Parasite {
        public double resourceNeeded;
        public String originHost;

        public Parasite(double resourceNeeded, String originHost){
            this.originHost = originHost;
            this.resourceNeeded = resourceNeeded;
        }
    }

    private final Map<String, Map<String, Double>> globalPheromoneMap;
    private final List<LocalPheromoneMap> populationList;

    private final Map<String, Host> hosts;
    private final Map<String, Parasite> parasites;
    private final Map<String, Host> underloadHosts;
    private final Map<String, Host> overloadHosts;

    private static final double localPheromoneVolatility = 0.3;
    private static final double globalPheromoneVolatility = 0.2;

    public double bestObjectValue = 0.0;
    private Map<String, String> bestMigrateMap = null;

    private static class Executor extends Thread {
        private final LocalPheromoneMap map;
        public Executor(LocalPheromoneMap map){
            this.map = map;
        }

        @Override
        public void run(){
            this.map.run();
        }
    }

    private static class LocalPheromoneMap {

        private final int antNum;
        private Map<String, Map<String, Double>> localPheromoneMap;
        private final double initialPheromoneConcentration;
        private final double explorationProbability;
        private final double pheromoneExponent;
        private final double heuristicExponent;

        private final Map<String, Host> hosts;
        private final Map<String, Parasite> parasites;

        private Map<String, String> bestMigrateMap;
        private double bestObjectValue = 0.0;

        private final AntColonySystem global;

        public Tuple2<Map<String, String>, Double> getBestMigrateMap(){
            return new Tuple2<>(this.bestMigrateMap, this.bestObjectValue);
        }

        public LocalPheromoneMap(
                int antNum,
                double initialPheromoneConcentration,
                double explorationProbability,
                Map<String, Parasite> parasites,
                Map<String, Host> hosts,
                double pheromoneExponent,
                double heuristicExponent,
                AntColonySystem global){

            this.localPheromoneMap = new ConcurrentHashMap<>();
            this.antNum = antNum;
            this.initialPheromoneConcentration = initialPheromoneConcentration;
            this.explorationProbability = explorationProbability;
            this.hosts = new HashMap<>();
            for(String hostname : hosts.keySet()){
                Host host = new Host(
                        hosts.get(hostname).used,
                        hosts.get(hostname).capacity,
                        hosts.get(hostname).parasiteNum);
                this.hosts.put(hostname, host);
            }
            this.parasites = parasites;
            this.heuristicExponent = heuristicExponent;
            this.pheromoneExponent = pheromoneExponent;
            this.global = global;
        }

        public void setLocalPheromoneMap(Map<String, Map<String, Double>> globalPheromoneMap){
            this.localPheromoneMap = new ConcurrentHashMap<>();
            for(String key : globalPheromoneMap.keySet()){
                Map<String, Double> tmp = new ConcurrentHashMap<>();
                this.localPheromoneMap.put(key, tmp);
                for(String _key : globalPheromoneMap.get(key).keySet()){
                    tmp.put(_key, globalPheromoneMap.get(key).get(_key));
                }
            }
        }

        public void run(){
            List<Map<String, String>> tmpMigrateMapList = new ArrayList<>();
            for(int i = 0; i < this.antNum; i++){
                Map<String, String> tmpMigrateMap = new HashMap<>();
                LinkedList<String> parasiteNames = new LinkedList<>(this.parasites.keySet());

                while (parasiteNames.size() > 0) {
                    int index = (int) (Math.random() * (double) parasiteNames.size());
                    String parasite = parasiteNames.get(index);
                    parasiteNames.remove(index);
                    String destHost = stateTransition(parasite);
                    this.hosts.get(destHost).tmp += this.parasites.get(parasite).resourceNeeded;
                    this.hosts.get(this.parasites.get(parasite).originHost).tmp
                            -= this.parasites.get(parasite).resourceNeeded;
                    tmpMigrateMap.put(parasite, destHost);
                }
                tmpMigrateMapList.add(tmpMigrateMap);
                for(String parasite : tmpMigrateMap.keySet()){
                    localPheromoneUpdate(parasite, tmpMigrateMap.get(parasite));
                }
                // 重置
                for(String hostname : this.hosts.keySet()){
                    this.hosts.get(hostname).tmp = this.hosts.get(hostname).used;
                }
            }

            for(Map<String, String> migrateMap : tmpMigrateMapList){
                double objectValue = global.objectFunction(migrateMap);
                if(objectValue > this.bestObjectValue){
                    this.bestMigrateMap = migrateMap;
                    this.bestObjectValue = objectValue;
                }
            }
        }

        public double stateTransition(String parasiteName, String destHost){
            double heuristic = heuristic(
                    parasiteName,
                    this.parasites.get(parasiteName).originHost,
                    destHost);
            double pheromone = this.localPheromoneMap.get(parasiteName).get(destHost);
            return Math.pow(pheromone, this.pheromoneExponent) *
                    Math.pow(heuristic, this.heuristicExponent);
        }

        public String stateTransition(String parasiteName){
            double random = Math.random();
            Map<String, Tuple2<Double, Double>> gTmp = new HashMap<>();
            double gMax = 0.0;
            if(random < this.explorationProbability){  // exploration
                double total = 0.0;
                for(String host : this.hosts.keySet()){
                    total += stateTransition(parasiteName, host);
                }

                for(String host : this.hosts.keySet()){
                    double p = stateTransition(parasiteName, host) / total;
                    gTmp.put(host, new Tuple2<>(gMax, p + gMax));
                    gMax += p;
                }

                double t = Math.random() * gMax;
                for(String host : gTmp.keySet()){
                    if(t >= gTmp.get(host).first && t < gTmp.get(host).second){
                        return host;
                    }
                }
                return null;
            }else{  // exploitation
                double max = 0.0;
                String destHost = null;
                if(!this.parasites.containsKey(parasiteName)){
                    LOGGER.error("[LocalPheromoneMap::stateTransition] parasite {} not exist",
                            parasiteName);
                    return null;
                }

                if(!this.localPheromoneMap.containsKey(parasiteName)){
                    LOGGER.error("[LocalPheromoneMap::stateTransition] parasite {} does not in the local pheromone map",
                            parasiteName);
                    return null;
                }

                for(String host : this.hosts.keySet()){
                    double tmp = stateTransition(parasiteName, host);
                    if(tmp > max){
                        max = tmp;
                        destHost = host;
                    }
                }
                return destHost;
            }
        }

        public double heuristic(String parasiteName, String originHost, String destHost){
            double heuristic = 0.0;
            double resourceNeeded;
            double line = 0.7;
            if(this.parasites.containsKey(parasiteName)){
                resourceNeeded = this.parasites.get(parasiteName).resourceNeeded;
                if(resourceNeeded + this.hosts.get(destHost).tmp > this.hosts.get(destHost).capacity){
                    return 0.0;
                }
            }else{
                LOGGER.error("Parasite {} does not exist", parasiteName);
                return 0.0;   // parasite not exist error
            }
            Host dest = this.global.hosts.get(destHost);
            if(originHost.equals(destHost)){
                return 1.0 / Math.abs((line - dest.tmp / dest.capacity));
            }else{
                return 1.0 / Math.abs((line - (dest.tmp + this.parasites.get(parasiteName).resourceNeeded) / dest.capacity));
            }



//            if(originHost.equals(destHost)){
//                if(this.global.getUnderloadHosts().containsKey(destHost)){
//                    heuristic = 2 * ((this.global.getUnderloadHosts().get(destHost).tmp) /
//                            this.global.getUnderloadHosts().get(destHost).capacity);
//                    if(heuristic > line){
//                        heuristic = 2 - heuristic;
//                    }
//                    return heuristic;
//                }else if(this.global.getOverloadHosts().containsKey(destHost)){
//                    heuristic = 2 * (1 - (this.global.getOverloadHosts().get(destHost).tmp /
//                            this.global.getOverloadHosts().get(destHost).capacity));
//                    if(heuristic > line){
//                        heuristic = 2 - heuristic;
//                    }
//                    return heuristic;
//                }else{
//                    LOGGER.error("host {} does not exist", originHost);
//                    return 0.0;
//                }
//            }
//
//            if(this.global.getUnderloadHosts().containsKey(originHost)){
//                heuristic += (this.global.getUnderloadHosts().get(originHost).tmp - resourceNeeded) /
//                        this.global.getUnderloadHosts().get(originHost).capacity;
//                if(heuristic > line){
//                    heuristic = 2 - heuristic;
//                }
//            }else if(this.global.getOverloadHosts().containsKey(originHost)){
//                heuristic += 1 - (this.global.getOverloadHosts().get(originHost).tmp - resourceNeeded) /
//                        this.global.getOverloadHosts().get(originHost).capacity;
//                if(heuristic > line){
//                    heuristic = 2 - heuristic;
//                }
//            }else{
//                LOGGER.error("Origin Host {} does not exist", originHost);
//                return 0.0;  // origin host not exist error
//            }
//
//            if(this.global.getUnderloadHosts().containsKey(destHost)){
//                heuristic += (this.global.getUnderloadHosts().get(destHost).tmp + resourceNeeded) /
//                        this.global.getUnderloadHosts().get(destHost).capacity;
//                if(heuristic > line){
//                    heuristic = 2 - heuristic;
//                }
//            }else if(this.global.getOverloadHosts().containsKey(destHost)){
//                heuristic += 1 - (this.global.getOverloadHosts().get(destHost).tmp + resourceNeeded) /
//                        this.global.getOverloadHosts().get(destHost).capacity;
//                if(heuristic > line){
//                    heuristic = 2 - heuristic;
//                }
//            }else{
//                LOGGER.error("Dest Host {} does not exist", destHost);
//                return 0.0;  // dest host not exist error
//            }
//
//            return heuristic;
        }

        public void localPheromoneUpdate(String parasiteName, String hostname){
            if(this.localPheromoneMap.containsKey(parasiteName)){
                Map<String, Double> tmp = this.localPheromoneMap.get(parasiteName);
                if(tmp.containsKey(hostname)){
                    Double newPheromoneConcentration = tmp.get(hostname) * (1 - AntColonySystem.localPheromoneVolatility) +
                            AntColonySystem.localPheromoneVolatility * this.initialPheromoneConcentration;
                    tmp.put(hostname, newPheromoneConcentration);
                }else{
                    LOGGER.error("Host name do not exist with {} in the local pheromone map", parasiteName);
                }
            }else{
                LOGGER.error("Parasite name do not exist in the local pheromone map");
            }
        }
    }


    public AntColonySystem(int populationNum,
                           int antNum,
                           double explorationProbability,
                           double pheromoneExponent,
                           double heuristicExponent,
                           Map<String, Host> underloadHosts,
                           Map<String, Host> overloadHosts,
                           Map<String, Parasite> parasites){

        this.globalPheromoneMap = new ConcurrentHashMap<>();
        this.populationList = new ArrayList<>();
        this.underloadHosts = underloadHosts;
        this.overloadHosts = overloadHosts;
        this.parasites = parasites;

        // 将过载主机与欠载主机整合到 `hosts' 中
        this.hosts = new ConcurrentHashMap<>();
        for(String hostname : this.underloadHosts.keySet()){
            this.hosts.put(hostname, this.underloadHosts.get(hostname));
        }
        for(String hostname : this.overloadHosts.keySet()){
            this.hosts.put(hostname, this.overloadHosts.get(hostname));
        }

        // 初始信息素浓度
        double initialPheromoneConcentration = (double) 1 / (double) parasites.size();

        // 初始化全局信息素矩阵
        for(String parasiteName : parasites.keySet()){
            this.globalPheromoneMap.put(parasiteName, new ConcurrentHashMap<>());
            for(String hostname : this.hosts.keySet()){
                this.globalPheromoneMap.get(parasiteName).put(hostname, initialPheromoneConcentration);
            }
        }

        // 初始化种群
        for(int i = 0; i < populationNum; i++){
            this.populationList.add(new LocalPheromoneMap(
                    antNum,
                    initialPheromoneConcentration,
                    explorationProbability,
                    this.parasites,
                    this.hosts,
                    pheromoneExponent,
                    heuristicExponent,
                    this));
        }
    }

    private int iterNum;

    public void setIterNum(int i){
        this.iterNum = i;
    }

    @Override
    public void run(){
        for(int i = 0; i < this.iterNum; i++){
            LOGGER.info("{} iteration start", i);
            runIteration();
        }
    }

    public void runIteration(){
        List<Executor> executors = new ArrayList<>();
        for(LocalPheromoneMap map : this.populationList){
            map.setLocalPheromoneMap(this.globalPheromoneMap);
            Executor executor = new Executor(map);
            executor.start();
            executors.add(executor);
        }

        for(Executor executor : executors){
            try {
                executor.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        for(LocalPheromoneMap map : this.populationList){
            boolean update = false;
            Tuple2<Map<String, String>, Double> best = map.getBestMigrateMap();
            if(best.second > this.bestObjectValue){
                LOGGER.info("Get a better object value {}", best.second);
                LOGGER.info("The migrate map is {}", best.first);

                this.bestObjectValue = best.second;
                this.bestMigrateMap = best.first;
                update = true;
            }
            if(update)
                globalPheromoneUpdate(this.bestMigrateMap, this.bestObjectValue);
            // else globalPheromoneUpdate();
        }
    }

    public void globalPheromoneUpdate(){
        for(String parasiteName : this.globalPheromoneMap.keySet()){
            Map<String, Double> tmp = this.globalPheromoneMap.get(parasiteName);
            for(String hostname : tmp.keySet()){
                double oldPheromoneConcentration = tmp.get(hostname);
                double newPheromoneConcentration =
                        (1 - AntColonySystem.globalPheromoneVolatility) * oldPheromoneConcentration;
                tmp.put(hostname, newPheromoneConcentration);
            }
        }
    }

    public void globalPheromoneUpdate(Map<String, String> migrateMap, double objectValue){
        for(String parasiteName : this.globalPheromoneMap.keySet()){
            Map<String, Double> tmp = this.globalPheromoneMap.get(parasiteName);
            String destHostname = migrateMap.getOrDefault(parasiteName, "");
            for(String hostname : tmp.keySet()){
                double oldPheromoneConcentration = tmp.get(hostname);
                double newPheromoneConcentration =
                        (1 - AntColonySystem.globalPheromoneVolatility) * oldPheromoneConcentration;
                if(hostname.equals(destHostname)){
                    newPheromoneConcentration +=
                            AntColonySystem.globalPheromoneVolatility * objectValue;
                }
                tmp.put(hostname, newPheromoneConcentration);
            }
        }
    }

    public double objectFunction(Map<String, String> migrateMap){
        int migrateNum = 0;
        double powerRatio = 0.0;
        Map<String, Host> migrateHosts = new HashMap<>();
        for(String parasiteName : migrateMap.keySet()){
            Parasite p = this.parasites.get(parasiteName);
            if(!p.originHost.equals(migrateMap.get(parasiteName))){
                migrateNum += 1;
                if(migrateHosts.containsKey(p.originHost)){
                    migrateHosts.get(p.originHost).used -= p.resourceNeeded;
                    migrateHosts.get(p.originHost).parasiteNum -= 1;
                }else{
                    Host tmp = this.hosts.get(p.originHost);
                    Host h = new Host(tmp.used, tmp.capacity, tmp.parasiteNum);
                    h.used -= p.resourceNeeded;
                    h.parasiteNum -= 1;
                    migrateHosts.put(p.originHost, h);
                }

                if(migrateHosts.containsKey(migrateMap.get(parasiteName))){
                    migrateHosts.get(migrateMap.get(parasiteName)).used += p.resourceNeeded;
                    migrateHosts.get(p.originHost).parasiteNum += 1;
                }else{
                    Host tmp = this.hosts.get(migrateMap.get(parasiteName));
                    Host h = new Host(tmp.used, tmp.capacity, tmp.parasiteNum);
                    h.used += p.resourceNeeded;
                    h.parasiteNum += 1;
                    migrateHosts.put(p.originHost, h);
                }
            }
        }



        for(String hostname : this.hosts.keySet()){
            Host h;
            if(migrateHosts.containsKey(hostname)){
                h = migrateHosts.get(hostname);
            }else{
                h = this.hosts.get(hostname);
            }
            if(h.parasiteNum != 0)
                powerRatio += h.used / h.capacity;
        }

        return (1 / (double) migrateNum) * 0.0 + 1 / powerRatio;
    }

    public Map<String, Host> getUnderloadHosts() {
        return underloadHosts;
    }

    public Map<String, Host> getOverloadHosts() {
        return overloadHosts;
    }

    public Map<String, String> getBestMigrateMap(){
        return this.bestMigrateMap;
    }

    public static void main(String[] args){
        Map<String, Host> underloadHosts = new HashMap<>();
        Map<String, Host> overloadHosts = new HashMap<>();
        Map<String, Parasite> parasites = new HashMap<>();

        underloadHosts.put("compute1", new Host(300, 1000, 2));
        parasites.put("container1", new Parasite(150.9, "compute1"));
        parasites.put("container2", new Parasite(101.3, "compute1"));

        underloadHosts.put("compute2", new Host(278, 1000, 2));
        parasites.put("container3", new Parasite(160, "compute2"));
        parasites.put("container4", new Parasite(101.56, "compute2"));

        overloadHosts.put("compute3", new Host(890, 1000, 5));
        parasites.put("container5", new Parasite(145.7, "compute3"));
        parasites.put("container6", new Parasite(229.6, "compute3"));
        parasites.put("container7", new Parasite(208.7, "compute3"));
        parasites.put("container8", new Parasite(178.3, "compute3"));
        parasites.put("container9", new Parasite(98.2, "compute3"));

        overloadHosts.put("compute4", new Host(1789, 2000, 8));
        parasites.put("container10", new Parasite(213.6, "compute4"));
        parasites.put("container11", new Parasite(314.7, "compute4"));
        parasites.put("container12", new Parasite(190.6, "compute4"));
        parasites.put("container13", new Parasite(200, "compute4"));
        parasites.put("container14", new Parasite(210.5, "compute4"));
        parasites.put("container15", new Parasite(139.2, "compute4"));
        parasites.put("container16", new Parasite(198.2, "compute4"));
        parasites.put("container17", new Parasite(200.1, "compute4"));

        underloadHosts.put("compute5", new Host(200, 2000, 1));
        parasites.put("container18", new Parasite(142.98, "compute5"));

        underloadHosts.put("compute6", new Host(310, 2000, 1));
        parasites.put("container19", new Parasite(279.2, "compute6"));

        underloadHosts.put("compute7", new Host(239.2, 1000, 1));
        parasites.put("container20", new Parasite(206.45, "compute7"));

        AntColonySystem antColonySystem = new AntColonySystem(
                50,
                1500,
                0.8,
                1,
                3,
                underloadHosts,
                overloadHosts,
                parasites
        );
        antColonySystem.setIterNum(50);
        antColonySystem.run();
        Map<String, String> bestMigrateMap = antColonySystem.getBestMigrateMap();
        LOGGER.info("result:");

        LOGGER.info("object value: {}", antColonySystem.bestObjectValue);
        for(String parasiteName : bestMigrateMap.keySet()){
            if(!parasites.get(parasiteName).originHost.equals(bestMigrateMap.get(parasiteName))){
                LOGGER.info("parasite {} migrate to host {}", parasiteName, bestMigrateMap.get(parasiteName));
            }
        }

        for(String parasiteName : bestMigrateMap.keySet()){
            if(!parasites.get(parasiteName).originHost.equals(bestMigrateMap.get(parasiteName))){
                Host destHost, srcHost;
                if(underloadHosts.containsKey(parasites.get(parasiteName).originHost)){
                    srcHost = underloadHosts.get(parasites.get(parasiteName).originHost);
                }else{
                    srcHost = overloadHosts.get(parasites.get(parasiteName).originHost);
                }

                if(underloadHosts.containsKey(bestMigrateMap.get(parasiteName))){
                    destHost = underloadHosts.get(bestMigrateMap.get(parasiteName));
                }else{
                    destHost = overloadHosts.get(bestMigrateMap.get(parasiteName));
                }

                srcHost.used -= parasites.get(parasiteName).resourceNeeded;
                destHost.used += parasites.get(parasiteName).resourceNeeded;
                srcHost.parasiteNum -= 1;
                destHost.parasiteNum += 1;
            }
        }

        for(String hostname : underloadHosts.keySet()){
            LOGGER.info("{} : {}", hostname, underloadHosts.get(hostname));
        }

        for(String hostname : overloadHosts.keySet()){
            LOGGER.info("{} : {}", hostname, overloadHosts.get(hostname));
        }
    }
}
