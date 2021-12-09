package com.github.fenrir.xopenstackagent.services.executors;

import com.alibaba.fastjson.JSONObject;
import com.github.fenrir.xcommon.actions.Action;
import com.github.fenrir.xcommon.actions.virtualmachines.VMLiveMigrateAction;
import com.github.fenrir.xopenstackagent.services.OpenStackVMOperationService;
import org.openstack4j.model.common.ActionResponse;

public class VMLiveMigrationExecutor implements ActionExecutor {

    private OpenStackVMOperationService vmOperationService;

    public VMLiveMigrationExecutor(OpenStackVMOperationService vmOperationService){
        this.setVmOperationService(vmOperationService);
    }

    @Override
    public ActionResponse doAction(Action<Object> objectAction){
        VMLiveMigrateAction.OptsData opts =
                ((JSONObject) objectAction.getOptions()).toJavaObject(VMLiveMigrateAction.OptsData.class);
        return vmOperationService.vmLiveMigrate(
                opts.getIsMigrateBlock(),
                true,
                opts.getDestHostname(),
                opts.getServerId()
        );
    }

    public OpenStackVMOperationService getVmOperationService(){
        return this.vmOperationService;
    }

    public void setVmOperationService(OpenStackVMOperationService service){
        this.vmOperationService = service;
    }
}
