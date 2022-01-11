package com.github.fenrir.xtraceprocessor.controllers;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.fenrir.xtraceprocessor.services.DependencyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping(path = "/api/v1/serviceDependency")
public class ServiceDependencyController {
    private final DependencyService service;

    public ServiceDependencyController(@Autowired DependencyService service){
        this.service = service;
    }

    /**
     * response
     * {
     *     "names": [ ... ]
     * }
     * @return json
     */
    @GetMapping(path = "/service/names")
    public JSONObject getServiceNames(){
        JSONObject ret = new JSONObject();
        ret.put("names", new JSONArray());
        ret.getJSONArray("names").addAll(this.service.getServiceNames());
        return ret;
    }

    /**
     * response
     * {
     *     "names": [ ... ]
     * }
     * @param serviceName service name
     * @return json
     */
    @GetMapping(path = "/service/{serviceName}/interface/names")
    public JSONObject getServiceInterfaceNames(@PathVariable(name = "serviceName") String serviceName){
        JSONObject ret = new JSONObject();
        ret.put("names", new JSONArray());
        Set<String> names = this.service.getServiceInterfaceNames(serviceName);
        if(names != null){
            ret.getJSONArray("names").addAll(names);
        }
        return ret;
    }

    /**
     * response
     * {
     *     "names": {
     *          "serviceName":[
     *              "interface1",
     *              "interface2"
     *          ],
     *
     *     }
     * }
     * request
     * {
     *     "interface": "xxx"
     * }
     * @param serviceName service name
     * @return json
     */
    @GetMapping(path = "/service/{serviceName}/interface/downstreamInterfaces/names", consumes = "application/json")
    public JSONObject getServiceInterfaceDownstreamInterfaceNames(@PathVariable(name = "serviceName") String serviceName,
                                                                  @RequestBody String body){
        JSONObject ret = new JSONObject();
        ret.put("names", new JSONObject());
        Map<String, Set<String>> names = this.service.getDownstreamInterfaceNames(serviceName,
                JSON.parseObject(body).getString("interface"));
        if(names != null){
            for(String downstreamServiceName: names.keySet()){
                JSONArray interfaces = new JSONArray();
                interfaces.addAll(names.get(downstreamServiceName));
                ret.getJSONObject("names").put(downstreamServiceName, interfaces);
            }
        }
        return ret;
    }
}
