package com.github.fenrir.xservertopologybuilder.services;

import com.github.fenrir.xcommon.utils.CommonUtils;
import com.github.fenrir.xservertopologybuilder.services.host.HostTopologyService;
import com.github.fenrir.xservertopologybuilder.services.vm.VMTopologyService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

@Service
public class Topology {

    @Getter @Setter private HostTopologyService hostTopologyService;
    @Getter @Setter private VMTopologyService vmTopologyService;

    // 循环检测 host map 和 vm map，将过久不更新的 host 或者 vm 删掉
    private static class StateCheckerTimerTask extends TimerTask {

        @Getter private static final long maxDelay = 10;

        @Getter @Setter private Map<String, HostTopologyService.Host> hostMap = null;
        @Getter @Setter private Map<String, VMTopologyService.VM> vmMap = null;

        public StateCheckerTimerTask(Map<String, HostTopologyService.Host> hostMap,
                                     Map<String, VMTopologyService.VM> vmMap){
            this.setHostMap(hostMap);
            this.setVmMap(vmMap);
        }

        @Override
        public void run() {
            for(String hostname : this.getHostMap().keySet()){
                HostTopologyService.Host host = this.getHostMap().get(hostname);
                long delay = CommonUtils.getTimestamp() - host.getTimestamp();
                if(delay >= getMaxDelay()){
                    this.getHostMap().remove(hostname);
                }
            }

            for(String vmUUID : this.getVmMap().keySet()){
                VMTopologyService.VM vm = this.getVmMap().get(vmUUID);
                long delay = CommonUtils.getTimestamp() - vm.getTimestamp();
                if(delay >= getMaxDelay()){
                    this.getVmMap().remove(vmUUID);
                }
            }
        }
    }

    public Topology(@Autowired HostTopologyService hostTopologyService,
                    @Autowired VMTopologyService vmTopologyService){
        this.setHostTopologyService(hostTopologyService);
        this.setVmTopologyService(vmTopologyService);

        Timer stateCheckerTimer = new Timer();
        stateCheckerTimer.schedule(
                new StateCheckerTimerTask(
                        this.getHostTopologyService().getHostMap(),
                        this.getVmTopologyService().getVmMap()
                ), 0, 2000
        );
    }
}
