package com.github.fenrir.xplanner.services;

import com.github.fenrir.xcommon.actions.Action;
import com.github.fenrir.xcommon.clients.xplanner.api.rest.entities.AddActionResponse;
import com.github.fenrir.xcommon.clients.xplanner.api.rest.entities.CreatePlanResponse;
import com.github.fenrir.xcommon.clients.xplanner.api.rest.entities.ExecuteResponse;
import com.github.fenrir.xcommon.utils.CommonUtils;
import com.github.fenrir.xplanner.objects.Plan;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PlannerService {
    // UUID string -> Plan
    @Getter private final Map<String, Plan> plansMap = new ConcurrentHashMap<>();

    @Getter @Setter private PlanExecutor executor;

    public enum State {
        PLAN_NOT_EXIST,
        START_RUNNING,
        BLOCKED,
        UNKNOWN
    }

    public PlannerService(@Autowired PlanExecutor executor){
        this.setExecutor(executor);
    }

    /**
     * 将一个 Action 加入一个指定的 Plan 中
     * @param planUUID plan uuid
     * @param action an action
     */
    public AddActionResponse putActionInPlan(String planUUID, Action<Object> action){
        AddActionResponse response = new AddActionResponse();
        Plan plan = this.getPlansMap().get(planUUID);
        if(plan == null) {
            response.status = 1;
            response.msg = "No such plan";
            return response;
        }
        plan.addAction(action);
        response.msg = "Success";
        response.status = 0;
        return response;
    }

    /**
     * 创建一个新的 Plan 放入 plansMap 并返回该 Plan 的 UUID<br>
     * <br>
     * @return plan uuid
     */
    public CreatePlanResponse createNewPlan(String agent){
        CreatePlanResponse response = new CreatePlanResponse();
        String uuid = CommonUtils.getUnusedUUID(
                this.getPlansMap().keySet());

        this.getPlansMap().put(uuid, new Plan(uuid, agent));
        response.uuid = uuid;
        return response;
    }

    /**
     * 执行一个 plan
     */
    public ExecuteResponse execute(String planUUID){
        ExecuteResponse response = new ExecuteResponse();
        if(!this.getPlansMap().containsKey(planUUID)){
            response.status = 2;
            response.msg = "Plan not exist";
            return response;
            // return State.PLAN_NOT_EXIST;
        }

        PlanExecutor.State state = this.getExecutor().execute(
                this.getPlansMap().get(planUUID)
        );

        if(state == PlanExecutor.State.RUNNING){
            this.getPlansMap().remove(planUUID);
            response.status = 0;
            response.msg = "Start running";
            return response;
            // return State.START_RUNNING;
        }else if(state == PlanExecutor.State.OTHER_PLAN_IS_RUNNING){
            response.status = 1;
            response.msg = "Other plan is running";
            return response;
            // return State.BLOCKED;
        }

        response.status = 3;
        response.msg = "Unknown error";
        return response;
        // return State.UNKNOWN;
    }
}
