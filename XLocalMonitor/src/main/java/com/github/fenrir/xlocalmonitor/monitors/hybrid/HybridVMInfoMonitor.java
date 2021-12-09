package com.github.fenrir.xlocalmonitor.monitors.hybrid;

import com.github.fenrir.xcommon.utils.CommonUtils;
import com.github.fenrir.xlocalmonitor.annotations.Monitor;
import com.github.fenrir.xlocalmonitor.inspectors.thirdpart.clients.libvirtclient.LibvirtAPI;
import com.github.fenrir.xlocalmonitor.inspectors.thirdpart.clients.netdataclient.NetdataAPI;
import com.github.fenrir.xlocalmonitor.inspectors.thirdpart.clients.netdataclient.metrics.NetdataCgroupQemuCpuUsageMetric;
import com.github.fenrir.xlocalmonitor.monitors.BaseMonitor;
import lombok.Getter;
import lombok.Setter;
import org.libvirt.Domain;
import org.libvirt.LibvirtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.TimerTask;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * use libvirt api to get all the Libvirt VM's vmName<br>
 * use netdata get the vm cpu usage from control group
 */
@Monitor(name = "HybridVMInfoMonitor", streams = {"vm.info"},
         inspectors = {"libvirt", "netdata"})
public class HybridVMInfoMonitor extends BaseMonitor {
    private static final Logger logger = LoggerFactory.getLogger(
            "HybridVMInfoMonitor");

    public static String getMonitorName(){
        return "HybridVMInfoMonitor";
    }

    @Getter @Setter private Lock lock = new ReentrantLock();
    @Getter @Setter private Condition stop = lock.newCondition();

    private static class HybridVMInfoMonitorTimerTask extends TimerTask {

        @Getter @Setter private HybridVMInfoMonitor monitor;
        @Getter @Setter private LibvirtAPI libvirtApi;
        @Getter @Setter private NetdataAPI netdataApi;


        public HybridVMInfoMonitorTimerTask(HybridVMInfoMonitor monitor,
                                            LibvirtAPI libvirtApi,
                                            NetdataAPI netdataApi){
            libvirtApi.connect();
            this.setMonitor(monitor);
            this.setLibvirtApi(libvirtApi);
            this.setNetdataApi(netdataApi);
        }

        @Override
        public void run() {
            // 获取当前时刻的 Libvirt 中的所有虚拟机ID（不是UUID）
            int[] domainIdList = libvirtApi.getAllActiveDomainIds();
            for(int id : domainIdList){ // 迭代处理每个 domain
                Domain domain = libvirtApi.getDomainById(id);
                if(domain != null){

                    String vmName;  // 虚拟机名称 instance-00000001
                    String vmUUID;  // 虚拟机UUID
                    int vCPUNum;    // 虚拟机 CPU 核数
                    long vMEMSize;  // 虚拟机内存大小

                    // 通过 netdata 的 cgroup_qemu_instance00000001.cpu chart 获取的 User CPU Usage
                    double cgroupCpuUsageUser;
                    // 通过 netdata 的 cgroup_qemu_instance00000001.cpu chart 获取的 System CPU Usage
                    double cgroupCpuUsageSystem;

                    try{
                        vmName = domain.getName();
                        vmUUID = domain.getUUIDString();
                        vCPUNum = domain.getMaxVcpus();
                        vMEMSize = domain.getMaxMemory();
                        cgroupCpuUsageUser = netdataApi.getCgroupQemuCpuUsage(
                                vmName
                        ).getLatestMetric(NetdataCgroupQemuCpuUsageMetric.Kind.USER, Double.class)
                                .getValue();

                        cgroupCpuUsageSystem = netdataApi.getCgroupQemuCpuUsage(
                                vmName
                        ).getLatestMetric(NetdataCgroupQemuCpuUsageMetric.Kind.SYSTEM, Double.class)
                                .getValue();

                    } catch (LibvirtException e){
                        e.printStackTrace();
                        continue;
                    }

                    // 上报数据
                    this.getMonitor().pushStreamData(
                            "vm.info",
                            this.getMonitor().createStreamData(
                                    "vm.info",
                                    this.getMonitor().getHostname(),
                                    vmName, vmUUID, vCPUNum, vMEMSize,
                                    cgroupCpuUsageUser, cgroupCpuUsageSystem,
                                    CommonUtils.getTimestamp()
                            )
                    );
                }
            }
        }
    }

    @Override
    public Map<String, Map<String, Object>> extract(){
        return null;
    }

    @Override
    protected void postStart() {
        logger.info("monitor stop running ... ");
    }

    @Override
    protected void doStart() {
        this.registerTimerTask(new HybridVMInfoMonitorTimerTask(
                this,
                (LibvirtAPI) this.getApiMap().get("libvirt"),
                (NetdataAPI) this.getApiMap().get("netdata")
        ), (long) 2000);

        lock.lock();
        try {
            stop.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void preStart() {
        // registerStream("vm.info");
        logger.info("monitor start running ... ");
    }

    @Override
    public void doStop() {
        this.stop.notify();
    }
}
