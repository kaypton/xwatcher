package com.github.fenrir.xplanner.controllers;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.fenrir.xcommon.actions.Action;
import com.github.fenrir.xplanner.objects.Plan;
import com.github.fenrir.xplanner.services.AgentService;
import com.github.fenrir.xplanner.services.PlanExecutor;
import com.github.fenrir.xplanner.services.PlannerService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping(path = "/xplanner/search")
public class SearchController {

    @Getter @Setter private PlanExecutor planExecutor;
    @Getter @Setter private PlannerService plannerService;

    public SearchController(@Autowired PlanExecutor planExecutor,
                            @Autowired PlannerService plannerService){
        this.setPlanExecutor(planExecutor);
        this.setPlannerService(plannerService);
    }

    @RequestMapping(path = "/executed_plan_info", method = RequestMethod.GET)
    public String getAllExecutedPlanInfo(){
        Map<Plan, Map<Action<Object>, AgentService.AgentExecuteResult>> executedPlanMap =
                this.getPlanExecutor().getExecutedPlansMap();
        JSONObject ret = new JSONObject();
        JSONArray planArray = new JSONArray();
        for(Plan plan : executedPlanMap.keySet()){
            JSONObject planJSON = new JSONObject();
            planJSON.put("uuid", plan.getUuid());
            JSONArray actionArray = new JSONArray();
            for(Action<Object> action : executedPlanMap.get(plan).keySet()){
                JSONObject actionJSON = new JSONObject();
                actionJSON.put("info", action);
                actionJSON.put("result", executedPlanMap.get(plan).get(action));
                actionArray.add(actionJSON);
            }
            planJSON.put("action", actionArray);
            planArray.add(planJSON);
        }

        ret.put("plans", planArray);
        return ret.toJSONString();
    }

    @RequestMapping(path = "/executing_plan_info", method = RequestMethod.GET)
    public String getAllExecutingPlanInfo(){
        Map<Plan, Map<Action<Object>, AgentService.AgentExecuteResult>> executingPlanMap =
                this.getPlanExecutor().getExecutedPlansMap();
        JSONObject ret = new JSONObject();
        JSONArray planArray = new JSONArray();
        for(Plan plan : executingPlanMap.keySet()){
            JSONObject planJSON = new JSONObject();
            planJSON.put("uuid", plan.getUuid());
            JSONArray actionArray = new JSONArray();
            for(Action<Object> action : executingPlanMap.get(plan).keySet()){
                JSONObject actionJSON = new JSONObject();
                actionJSON.put("info", action);
                actionJSON.put("result", executingPlanMap.get(plan).get(action));
                actionArray.add(actionJSON);
            }
            planJSON.put("actions", actionArray);
            planArray.add(planJSON);
        }

        ret.put("plans", planArray);
        return ret.toJSONString();
    }

    @RequestMapping(path = "/queueing_plan_info", method = RequestMethod.GET)
    public String getAllQueueingPlanInfo(){
        Map<String, Plan> planMap = this.getPlannerService().getPlansMap();
        JSONObject ret = new JSONObject();
        JSONArray planArray = new JSONArray();

        for(String uuid : planMap.keySet()){
            Plan plan = planMap.get(uuid);
            Map<String, Action<Object>> actionList = plan.getActionList();
            JSONObject planJSON = new JSONObject();
            planJSON.put("uuid", uuid);
            JSONArray actionArray = new JSONArray();
            for(String actionUUID : actionList.keySet()){
                JSONObject actionJSON = new JSONObject();
                actionJSON.put("uuid", actionUUID);
                actionJSON.put("info", actionList.get(actionUUID));
                actionArray.add(actionJSON);
            }
            planJSON.put("actions", actionArray);
            planArray.add(planJSON);
        }

        ret.put("plans", planArray);
        return ret.toJSONString();
    }
}
