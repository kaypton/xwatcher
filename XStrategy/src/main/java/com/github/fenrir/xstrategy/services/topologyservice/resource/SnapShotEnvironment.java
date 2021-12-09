package com.github.fenrir.xstrategy.services.topologyservice.resource;

import lombok.Getter;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 在进行一次蚁群调度算法时，CurrentEnvironment类负责存储当前系统的快照状态<br/>
 * 并提供测试迁移方案有效性的功能（testMigrationPlan）
 */
public class SnapShotEnvironment {
    @Getter private final Map<String, VirtualMachine> vmMap;
    @Getter private final Map<String, Host> hostMap;
    @Getter private final Map<VirtualMachine, Host> currentHostVmAssignmentMap;
    @Getter private final Map<String, String> currentHostVmIdAssignmentMap;
    @Getter private final Map<Host, Set<VirtualMachine>> treeMap;

    public SnapShotEnvironment(){
        this.currentHostVmAssignmentMap = new ConcurrentHashMap<>();
        this.currentHostVmIdAssignmentMap = new ConcurrentHashMap<>();
        this.vmMap = new ConcurrentHashMap<>();
        this.hostMap = new ConcurrentHashMap<>();
        this.treeMap = new ConcurrentHashMap<>();
    }

    public void add(Host host, VirtualMachine virtualMachine){
        if(!this.getCurrentHostVmAssignmentMap().containsKey(virtualMachine))
            this.getCurrentHostVmAssignmentMap().put(virtualMachine, host);

        if(!this.getVmMap().containsKey(virtualMachine.getVmUUID()))
            this.getVmMap().put(virtualMachine.getVmUUID(), virtualMachine);

        if(!this.getHostMap().containsKey(host.getHostname()))
            this.getHostMap().put(host.getHostname(), host);

        if(!this.getTreeMap().containsKey(host)){
            Set<VirtualMachine> vmSet = new HashSet<>();
            vmSet.add(virtualMachine);
            this.getTreeMap().put(host, vmSet);
        }else{
            this.getTreeMap().get(host).add(virtualMachine);
        }

    }

    /**
     * 测试迁移计划的可行性，并给出评价指标值<br/>
     * 在蚁群算法中，这个函数可以理解为是计算目标函数值<br/>
     * @param assignedMap 虚拟机分配方案
     * @return 评价指标
     */
    public Double testMigrationPlan(Map<String, String> assignedMap){
        int migrationNum = this.countMigrationNum(assignedMap);
        Map<Host, List<VirtualMachine>> hvMap = convert(assignedMap);
        List<Double> idleRatioList = new ArrayList<>();

        for(Host host : hvMap.keySet()){
            List<VirtualMachine> vms = hvMap.get(host);
            int vcpus = 0;
            long vmem = 0;
            Double vmCpuUsage = 0.0;
            for(VirtualMachine vm : vms){
                vcpus += vm.getVcpus();
                vmem += vm.getMaxMem();
                vmCpuUsage += vm.getCpuUsage();
            }

            // 虚拟机申请的 cpu 核数或者内存数超标了
            if(vcpus > host.getCpus() || vmem > host.getMem())
                return 0.0;

            Double hostCpuIdleUsage = host.getIdleCpuUsage();
            Double idleRatio = hostCpuIdleUsage / (hostCpuIdleUsage + vmCpuUsage);
            idleRatioList.add(idleRatio);
        }

        // 计算评分
        // 迁移数量评分
        Double migrationScore = ((double) 1) / ((double) migrationNum);

        // CPU idle比评分
        Double idleRatioSum = 0.0;
        for(Double i : idleRatioList){
            idleRatioSum += i;
        }
        Double idleRatioScore = ((double) 1) / idleRatioSum;
        return idleRatioScore + migrationScore;
    }

    private Map<Host, List<VirtualMachine>> convert(Map<String, String> assignedMap){
        Map<Host, List<VirtualMachine>> ret = new ConcurrentHashMap<>();
        for(String vmUUID : assignedMap.keySet()){
            Host host = this.getHostMap().get(assignedMap.get(vmUUID));
            if(!ret.containsKey(host))
                ret.put(host, new ArrayList<>());
            ret.get(host).add(this.getVmMap().get(vmUUID));
        }
        return ret;
    }

    /**
     * 获取当前环境快照中的虚拟机总数
     * @return 虚拟机总数
     */
    public Integer getVirtualMachineNum(){
        return this.getVmMap().size();
    }

    /**
     * 获取当前快照环境中的物理机总数
     * @return 物理机总数
     */
    public Integer getHostNum(){
        return this.getHostMap().size();
    }

    /**
     * 获取当前快照环境中的物理机 CPU 核心总数
     * @return CPU 核心总数
     */
    public Integer getHostCpuCoreNum(){
        int coreNum = 0;
        for(String hostname : this.getHostMap().keySet()){
            coreNum += this.getHostMap().get(hostname).getCpus();
        }
        return coreNum;
    }

    /**
     * 获取当前快照环境中的虚拟机虚拟 CPU 核心总数
     * @return 虚拟 CPU 核心总数
     */
    public Integer getVirtualMachineVCpuCoreNum(){
        int coreNum = 0;
        for(String vmUUID : this.getVmMap().keySet()){
            coreNum += this.getVmMap().get(vmUUID).getVcpus();
        }
        return coreNum;
    }

    private int countMigrationNum(Map<String, String> assignedMap){
        int count = 0;
        for(String vmUUID : assignedMap.keySet()){
            if(!this.getCurrentHostVmIdAssignmentMap().containsKey(vmUUID)){
                if(this.getCurrentHostVmIdAssignmentMap().get(vmUUID).equals(
                        assignedMap.get(vmUUID)
                ))
                    count += 1;
            }
        }
        return count;
    }

    public static SnapShotEnvironment build(){
        return new SnapShotEnvironment();
    }
}
