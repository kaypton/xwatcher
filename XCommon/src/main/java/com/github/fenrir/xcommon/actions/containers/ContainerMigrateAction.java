package com.github.fenrir.xcommon.actions.containers;

import com.alibaba.fastjson.JSON;
import com.github.fenrir.xcommon.actions.Action;
import lombok.Getter;
import lombok.Setter;

public class ContainerMigrateAction extends Action<ContainerMigrateAction.OptsData> {

    @Override
    public String getActionName() {
        return actionName;
    }

    public ContainerMigrateAction(OptsData opts){
        super(opts);
    }

    @Override
    public OptsData getOptions(){
        return this.options;
    }

    @Override
    public void setOptions(OptsData opts){
        this.options = opts;
    }

    @Override
    public String toJSONString(){
        return JSON.toJSONString(this);
    }

    public String actionName = Action.CONTAINER_MIGRATE;

    public static class OptsData {
        /**
         * container id
         */
        @Getter @Setter private String containerId;

        /**
         * 目的主机主机名
         */
        @Getter @Setter private String destHostname;
    }
}
