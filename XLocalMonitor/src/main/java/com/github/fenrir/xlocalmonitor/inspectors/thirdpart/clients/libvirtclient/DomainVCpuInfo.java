package com.github.fenrir.xlocalmonitor.inspectors.thirdpart.clients.libvirtclient;

import lombok.Getter;
import lombok.Setter;
import org.libvirt.VcpuInfo;

public class DomainVCpuInfo {

    private static enum CpuState {
        OFFLINE, RUNNING, BLOCKED
    }

    @Getter @Setter private VcpuInfo vcpuInfo;
    public DomainVCpuInfo(VcpuInfo vcpuInfo){
        this.setVcpuInfo(vcpuInfo);
    }

    public long getCpuTime(){
        return this.getVcpuInfo().cpuTime;
    }

    public VcpuInfo.VcpuState getCpuState(){
        return this.getVcpuInfo().state;
    }
}
