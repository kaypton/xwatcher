package com.github.fenrir.xstrategy.services.topologyservice.resource;

import lombok.Getter;
import lombok.Setter;

public class VirtualMachine {
    /**
     * 虚拟机 UUID
     */
    @Getter @Setter private String vmUUID;

    /**
     * 所属物理机的主机名
     */
    @Getter @Setter private String hostname;

    /**
     * 虚拟机 CPU 使用量（从 control group 中获取）</br>
     * 在当前版本中此量为 cpu user usage + cpu system usage
     */
    @Getter @Setter private Double cpuUsage;

    /**
     * 虚拟机所需的 CPU 核数
     */
    @Getter @Setter private Integer vcpus;

    /**
     * 虚拟机所需的内存大小
     */
    @Getter @Setter private Long maxMem;

    public VirtualMachine(String vmUUID,
                          String hostname,
                          Double cpuUsage,
                          Integer vcpus,
                          Long maxMem){

        this.setVmUUID(vmUUID);
        this.setHostname(hostname);
        this.setCpuUsage(cpuUsage);
        this.setVcpus(vcpus);
        this.setMaxMem(maxMem);
    }
}
