package com.github.fenrir.prometheusdata;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/v1/stats/prometheus")
public class DataContainerController {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataContainerController.class);

    private final DataContainerService dataContainerService;
    public DataContainerController(@Autowired DataContainerService dataContainerService){
        this.dataContainerService = dataContainerService;
        LOGGER.info("DataContainerController ...");
    }

    @GetMapping(path = "/{name}", produces = "text/plain")
    public String metrics(@PathVariable(name = "name") String name){
        return this.dataContainerService.getMetricPlainTextString(name);
    }
}
