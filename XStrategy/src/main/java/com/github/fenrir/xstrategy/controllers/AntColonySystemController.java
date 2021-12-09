package com.github.fenrir.xstrategy.controllers;

import com.github.fenrir.xcommon.clients.BaseResponse;
import com.github.fenrir.xstrategy.services.strategies.StrategyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/v1/test/strategy/acs")
public class AntColonySystemController {
    private final StrategyService service;

    public AntColonySystemController(@Autowired @Qualifier("antColonySystem") StrategyService service){
        this.service = service;
        this.service.startup();  // Startup AntColonySystem strategy metric collecting module
    }

    @GetMapping(path = "/trigger")
    public BaseResponse trigger(){
        return this.service.trigger();
    }

    @GetMapping(path = "/result")
    public BaseResponse result(){
        return this.service.result();
    }

    @GetMapping(path = "/test")
    public BaseResponse test(){
        return this.service.test();
    }
}
