package com.github.fenrir.xcommon.actions.virtualmachines;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.github.fenrir.xcommon.actions.Action;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * virtual machine live migrate action<br/>
 */
public class VMLiveMigrateAction extends Action<VMLiveMigrateAction.OptsData> {

    @Override
    public String getActionName() {
        return actionName;
    }

    public VMLiveMigrateAction(OptsData opts){
        super(opts);
    }

    @Override
    public OptsData getOptions(){
        return this.options;
    }

    @Override
    public void setOptions(OptsData opts){
        if(checkOpts(opts)){
            this.options = new OptsData();

            this.options.setServerId(opts.serverId);
            this.options.setDestHostname(opts.destHostname);
            this.options.setIsMigrateBlock(opts.isMigrateBlock);
        }
    }

    private Boolean checkOpts(OptsData opts){
        return opts.serverId != null && opts.destHostname != null &&
                opts.isMigrateBlock != null;
    }

    @Override
    public String toJSONString(){
        return JSON.toJSONString(this);
    }

    public String actionName = Action.VM_LIVE_MIGRATE;

    public static class OptsData {
        /**
         * 源虚拟机 ID
         */
        @Getter @Setter private String serverId;

        /**
         * 目的主机主机名
         */
        @Getter @Setter private String destHostname;

        /**
         * 是否迁移块设备
         */
        @Getter @Setter private Boolean isMigrateBlock;
    }
}
