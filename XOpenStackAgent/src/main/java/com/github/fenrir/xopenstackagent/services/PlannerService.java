package com.github.fenrir.xopenstackagent.services;

import com.github.fenrir.xcommon.actions.Action;
import com.github.fenrir.xopenstackagent.services.executors.ActionExecutor;
import com.github.fenrir.xopenstackagent.services.executors.VMLiveMigrationExecutor;
import lombok.Getter;
import lombok.Setter;
import org.openstack4j.model.common.ActionResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PlannerService {

    private static final Map<String, Class<? extends ActionExecutor>> executorMap =
            new ConcurrentHashMap<>();

    static {
        executorMap.put(Action.VM_LIVE_MIGRATE, VMLiveMigrationExecutor.class);
    }

    private static ActionExecutor getActionExecutor(String actionName,
                                                    OpenStackVMOperationService vmOperationService){
        try{
            return executorMap.get(actionName).getDeclaredConstructor(
                    OpenStackVMOperationService.class
            ).newInstance(vmOperationService);
        }  catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e){
            e.printStackTrace();
            return null;
        }
    }

    @Getter @Setter private OpenStackVMOperationService vmOperationService;

    public PlannerService(@Autowired OpenStackVMOperationService vmOperationService){
        this.setVmOperationService(vmOperationService);
    }

    public ActionResponse executeAction(Action<Object> objectAction){
        ActionExecutor executor = getActionExecutor(objectAction.getActionName(),
                this.getVmOperationService());
        if(executor != null) {
            return executor.doAction(objectAction);
        }else{
            return null;
        }
    }
}
