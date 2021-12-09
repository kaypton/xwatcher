package com.github.fenrir.xopenstackagent.controllers;

import com.github.fenrir.xcommon.actions.Action;
import com.github.fenrir.xopenstackagent.services.PlannerService;
import io.swagger.annotations.ApiOperation;
import lombok.Getter;
import lombok.Setter;
import org.openstack4j.model.common.ActionResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Xu DiShi
 */
@RestController
@RequestMapping(path = "/planner")
public class PlannerController {

    @Getter @Setter private PlannerService plannerService;

    public PlannerController(@Autowired PlannerService plannerService){
        this.setPlannerService(plannerService);
    }

    @ApiOperation("XPlanner interface")
    @RequestMapping(path = "/doAction",
            method = RequestMethod.POST,
            produces = "application/json;charset=UTF-8")
    public ActionResponse doAction(@RequestBody Action<Object> objectAction){
        return this.getPlannerService().executeAction(objectAction);
    }
}
