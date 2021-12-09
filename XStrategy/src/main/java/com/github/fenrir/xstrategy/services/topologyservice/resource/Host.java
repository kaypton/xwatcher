package com.github.fenrir.xstrategy.services.topologyservice.resource;

import lombok.Getter;
import lombok.Setter;

public class Host {
    @Getter @Setter private String hostname;

    /**
     * idle cpu 使用量，在当前环境下指的是除了 VM 以外的其他所有 Application 的</br>
     * cpu 使用量</br>
     * 在 netdata 中可以通过 chart=apps.cpu 来获取数据
     */
    @Getter @Setter private Double idleCpuUsage;

    /**
     * 虚拟机 CPU 使用量</br>
     * 通过 netdata 中 chart=apps.cpu 来获取数据
     */
    @Getter @Setter private Double vmCpuUsage;

    /**
     * 主机 cpu 核数
     */
    @Getter @Setter private Integer cpus;

    /**
     * 主机内存大小
     */
    @Getter @Setter private Long mem;

    public Host(String hostname,
                Double idleCpuUsage,
                Double vmCpuUsage,
                Integer cpus,
                Long mem){

        this.setHostname(hostname);
        this.setCpus(cpus);
        this.setIdleCpuUsage(idleCpuUsage);
        this.setVmCpuUsage(vmCpuUsage);
        this.setMem(mem);
    }
}
