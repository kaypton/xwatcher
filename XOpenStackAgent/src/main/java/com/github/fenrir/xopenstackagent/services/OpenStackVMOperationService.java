package com.github.fenrir.xopenstackagent.services;

import lombok.Getter;
import lombok.Setter;
import org.openstack4j.model.common.ActionResponse;
import org.openstack4j.model.compute.actions.LiveMigrateOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OpenStackVMOperationService {

    @Getter @Setter private OpenStackAgentService agentService;

    public OpenStackVMOperationService(@Autowired OpenStackAgentService agentService){
        this.setAgentService(agentService);
    }

    /**
     * 虚拟机热迁移操作
     * @param blockMigration 是否迁移块设备
     * @param diskOverCommit 是否允许磁盘超载
     * @param host 目的主机名
     * @param serverId 虚拟机 ID
     * @return ActionResponse
     */
    public ActionResponse vmLiveMigrate(boolean blockMigration,
                                        boolean diskOverCommit,
                                        String host,
                                        String serverId){
        LiveMigrateOptions liveMigrateOptions = LiveMigrateOptions.create()
                .blockMigration(blockMigration)
                .diskOverCommit(diskOverCommit)
                .host(host);
        return this.getAgentService().getOSClient().compute().servers().liveMigrate(
                        serverId, liveMigrateOptions);

    }
}
