package com.github.fenrir.xplanner.controllers;

import com.alibaba.fastjson.JSON;
import com.github.fenrir.xcommon.actions.Action;
import com.github.fenrir.xcommon.clients.xplanner.api.rest.entities.AddActionRequest;
import com.github.fenrir.xcommon.clients.xplanner.api.rest.entities.AddActionResponse;
import com.github.fenrir.xcommon.clients.xplanner.api.rest.entities.CreatePlanResponse;
import com.github.fenrir.xcommon.clients.xplanner.api.rest.entities.ExecuteResponse;
import com.github.fenrir.xplanner.services.PlannerService;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path="/xplanner")
public class XPlannerController {
    private static Logger LOGGER = LoggerFactory.getLogger(XPlannerController.class);

    private final PlannerService plannerService;

    public XPlannerController(@Autowired PlannerService plannerService){
        this.plannerService = plannerService;
    }

    @ApiOperation("create a plan")
    @RequestMapping(path="/create/{agent}", method=RequestMethod.GET)
    public CreatePlanResponse createNewPlan(@PathVariable("agent") String agent){
        return this.getPlannerService().createNewPlan(agent);
    }

    @ApiOperation("Add an action into a plan")
    @RequestMapping(path="/add_action",
            method=RequestMethod.POST,
            produces="application/json;charset=UTF-8")
    public AddActionResponse addActionToPlan(@RequestBody AddActionRequest addActionRequest){
        LOGGER.info(JSON.toJSONString(addActionRequest));
        Action<Object> objectAction = Action.createAction(addActionRequest.actionName, addActionRequest.actionOpts);

        return this.getPlannerService().putActionInPlan(
                addActionRequest.planUUID,
                objectAction);
    }

    @ApiOperation("execute a plan")
    @RequestMapping(path="/execute_plan", method=RequestMethod.GET)
    public ExecuteResponse executePlan(@RequestParam String planUuid){
        return this.getPlannerService().execute(planUuid);
    }

    private PlannerService getPlannerService() {
        return plannerService;
    }
}
