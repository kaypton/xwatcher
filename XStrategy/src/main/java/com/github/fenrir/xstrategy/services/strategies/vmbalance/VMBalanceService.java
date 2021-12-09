package com.github.fenrir.xstrategy.services.strategies.vmbalance;

import com.github.fenrir.xcommon.actions.virtualmachines.VMLiveMigrateAction;
import com.github.fenrir.xstrategy.services.planservice.PlanService;
import com.github.fenrir.xstrategy.services.topologyservice.TopologyService;
import com.github.fenrir.xstrategy.services.topologyservice.resource.Host;
import com.github.fenrir.xstrategy.services.topologyservice.resource.SnapShotEnvironment;
import com.github.fenrir.xstrategy.services.topologyservice.resource.VirtualMachine;
import com.github.fenrir.xcommon.actions.Action;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Service
public class VMBalanceService implements Runnable {

    public enum State {
        ALREADY_RUNNING,
        RUNNING
    }

    @Getter private final ThreadPoolExecutor vmBalanceExecutor =
            new ThreadPoolExecutor(
                    1, 1, 0, TimeUnit.SECONDS, new LinkedBlockingQueue<>());

    @Getter @Setter private PlanService planService;
    @Getter @Setter private TopologyService topologyService;

    public VMBalanceService(@Autowired PlanService planService,
                            @Autowired TopologyService topologyService){
        this.setPlanService(planService);
        this.setTopologyService(topologyService);
    }

    public State trigger(){
        synchronized (this) {
            if(getVmBalanceExecutor().getActiveCount() != 0){
                return State.ALREADY_RUNNING;
            }else{
                getVmBalanceExecutor().execute(this);
                return State.RUNNING;
            }
        }
    }

    @Override
    public void run() {
        SnapShotEnvironment snapShotEnvironment =
                this.getTopologyService().getEntireSnapShotEnvironment();

        int vmNum = snapShotEnvironment.getVirtualMachineNum();
        int hostNum = snapShotEnvironment.getHostNum();

        double vmNumPerHost = (double) vmNum / (double) hostNum;

        Set<VirtualMachine> toMigrate = new HashSet<>();
        Set<Host> migrateDest = new HashSet<>();

        Map<Host, Set<VirtualMachine>> hostTreeMap = snapShotEnvironment.getTreeMap();

        // 提取欠分配主机，并提取需要被迁移的虚拟机
        for(Host host : hostTreeMap.keySet()){
            int subVmNum = hostTreeMap.get(host).size();
            if((double) subVmNum < vmNumPerHost){
                migrateDest.add(host);
                continue;
            }
            for(VirtualMachine vm : hostTreeMap.get(host)){
                if((double) subVmNum > vmNumPerHost){
                    toMigrate.add(vm);
                    subVmNum -= 1;
                }else{
                    break;
                }
            }
        }

        String planUUID = this.getPlanService().createNewPlan("openstack");

        // 将提取出来的虚拟机分配到欠分配的主机上
        for(Host host : migrateDest){
            int subVmNum = hostTreeMap.get(host).size();
            Set<VirtualMachine> tmpRemove = new HashSet<>();
            for(VirtualMachine vm : toMigrate){
                if((double) subVmNum < vmNumPerHost){

                    VMLiveMigrateAction.OptsData migrateOpts = new VMLiveMigrateAction.OptsData();
                    migrateOpts.setServerId(vm.getVmUUID());
                    migrateOpts.setDestHostname(host.getHostname());
                    migrateOpts.setIsMigrateBlock(false);

                    Action<VMLiveMigrateAction.OptsData> migrateAction = Action.createAction(
                            migrateOpts,
                            VMLiveMigrateAction.class,
                            VMLiveMigrateAction.OptsData.class
                    );

                    this.getPlanService().addActionToPlan(planUUID, migrateAction);
                    tmpRemove.add(vm);
                    subVmNum += 1;
                }else{
                    break;
                }
            }
            toMigrate.removeAll(tmpRemove);
        }

        this.getPlanService().executePlan(planUUID);
    }

}
