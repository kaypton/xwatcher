package com.github.fenrir.xstrategy.services.planservice;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.fenrir.xstrategy.restapis.XPlannerRestAPI;
import com.github.fenrir.xcommon.actions.Action;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PlanService {

    @Getter @Setter private XPlannerRestAPI xPlannerRestAPI;

    public PlanService(@Autowired XPlannerRestAPI xPlannerRestAPI){
        this.setXPlannerRestAPI(xPlannerRestAPI);
    }

    public String createNewPlan(String agent){
        JSONObject uuidJSON = JSON.parseObject(
                this.getXPlannerRestAPI().createNewPlan(agent));
        return uuidJSON.getString("uuid");
    }

    public void addActionToPlan(String planUUID, Action<?> action){
        this.getXPlannerRestAPI().addActionToPlan(planUUID, action);
    }

    public void executePlan(String planUUID){
        this.getXPlannerRestAPI().executePlan(planUUID);
    }
}
