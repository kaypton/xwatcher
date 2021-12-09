package com.github.fenrir.xcommon.actions;

import com.alibaba.fastjson.JSON;

public class ObjectAction extends Action<Object> {

    public Object options;

    private String actionName;

    public ObjectAction(Object opts) {
        super(opts);
    }

    public void setActionName(String actionName){
        this.actionName = actionName;
    }

    @Override
    public String getActionName() {
        return this.actionName;
    }

    @Override
    public void setOptions(Object opts) {
        this.options = opts;
    }

    @Override
    public Object getOptions() {
        return this.options;
    }

    @Override
    public String toJSONString() {
        return JSON.toJSONString(this);
    }
}
