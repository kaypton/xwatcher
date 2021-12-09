package com.github.fenrir.xstrategy.controllers;

import com.github.fenrir.xcommon.clients.xstrategy.api.rest.entities.VMBalanceTriggerResponse;
import com.github.fenrir.xstrategy.services.strategies.vmbalance.VMBalanceService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/v1/test/strategy/vm_balance")
public class VMBalanceController {

    @Getter @Setter private VMBalanceService service;

    public VMBalanceController(@Autowired VMBalanceService service){
        this.setService(service);
    }

    @RequestMapping(path = "/trigger")
    public VMBalanceTriggerResponse trigger(){
        VMBalanceTriggerResponse response = new VMBalanceTriggerResponse();
        VMBalanceService.State state = service.trigger();
        if(state == VMBalanceService.State.ALREADY_RUNNING){
            response.status = 1;
            response.msg = "Already running";
        }else if(state == VMBalanceService.State.RUNNING){
            response.status = 0;
            response.msg = "Running";
        }else{
            response.status = 2;
            response.msg = "Unknown state";
        }
        return response;
    }
}
