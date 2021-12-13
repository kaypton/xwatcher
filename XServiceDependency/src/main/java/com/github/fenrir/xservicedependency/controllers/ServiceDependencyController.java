package com.github.fenrir.xservicedependency.controllers;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.fenrir.xcommon.utils.Tuple2;
import com.github.fenrir.xcommon.utils.Tuple3;
import com.github.fenrir.xservicedependency.entities.serviceDependency.Interface;
import com.github.fenrir.xservicedependency.entities.serviceDependency.Service;
import com.github.fenrir.xservicedependency.services.DependencyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedList;
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
        ret.getJSONArray("names").addAll(this.service.getServiceMap().keySet());
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
     *     "interfaces": [
     *          {
     *              "name": "xxx",
     *              "serviceTime": [
     *                  {
     *                      "startUnixTimeNano": 1234,
     *                      "endUnixTimeNano": 1235,
     *                      "serviceTimeNano": 1
     *                  },
     *                  ...
     *              ]
     *          }
     *     ]
     * }
     * @param serviceName service name
     * @return json
     */
    @GetMapping(path = "/service/{serviceName}/interfaces")
    public JSONObject getInterfaces(@PathVariable(name = "serviceName") String serviceName){
        JSONObject ret = new JSONObject();
        ret.put("interfaces", new JSONArray());
        Service srv = this.service.getServiceMap().getOrDefault(serviceName, null);
        if(srv != null){
            for(String interfaceName : srv.getInterfaceMap().keySet()){
                Interface i = srv.getInterface(interfaceName);
                if(i != null){
                    JSONObject obj = new JSONObject();
                    obj.put("name", i.getName());
                    obj.put("serviceTime", new JSONArray());
                    for(Tuple3<Long, Long, Long> time : i.getServiceTimeNanoList()){
                        JSONObject _obj = new JSONObject();
                        _obj.put("startUnixTimeNano", time.first);
                        _obj.put("endUnixTimeNano", time.second);
                        _obj.put("serviceTimeNano", time.third);
                        obj.getJSONArray("serviceTime").add(_obj);
                    }
                    ret.getJSONArray("interfaces").add(obj);
                }
            }
        }
        return ret;
    }

    /**
     * {
     *     "name": "xxx",
     *     "serviceTime": [
     *          {
     *              "startUnixTimeNano": 1234,
     *              "endUnixTimeNano": 1235,
     *              "serviceTimeNano": 1
     *          },
     *          ...
     *     ]
     * }
     * request
     * {
     *     "interface": "xxx"
     * }
     * @param serviceName service name
     * @param body interface name
     * @return json
     */
    @GetMapping(path = "/service/{serviceName}/interface", consumes = "application/json")
    public JSONObject getServiceInterface(@PathVariable(name = "serviceName") String serviceName,
                                          @RequestBody String body){
        String interfaceName = JSON.parseObject(body).getString("interface");
        JSONObject ret = new JSONObject();
        ret.put("name", interfaceName);
        ret.put("serviceTime", new JSONArray());

        Service srv = this.service.getServiceMap().getOrDefault(serviceName, null);
        if(srv != null){
            Interface i = srv.getInterface(interfaceName);
            if(i != null){
                for(Tuple3<Long, Long, Long> time : i.getServiceTimeNanoList()){
                    JSONObject _obj = new JSONObject();
                    _obj.put("startUnixTimeNano", time.first);
                    _obj.put("endUnixTimeNano", time.second);
                    _obj.put("serviceTimeNano", time.third);
                    ret.getJSONArray("serviceTime").add(_obj);
                }
            }
        }

        return ret;
    }

    /**
     * response
     * {
     *     "names": [
     *          "serviceName:interfaceName",
     *          ...
     *     ]
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
        ret.put("names", new JSONArray());
        Set<String> names = this.service.getServiceInterfaceDownstreamInterfaceNames(serviceName,
                JSON.parseObject(body).getString("interface"));
        if(names != null){
            ret.getJSONArray("names").addAll(names);
        }
        return ret;
    }

    /**
     * response
     * {
     *     "interfaces": [
     *          {
     *              "name": "serviceName:interfaceName",
     *              "respTime": [
     *                  {
     *                      "startUnixTimeNano": 1234,
     *                      "endUnixTimeNano": 1235,
     *                      "responseTimeNano": 1
     *                  },
     *                  ...
     *              ]
     *          }
     *          ...
     *     ]
     * }
     * request
     * {
     *     "interface": "xxx"
     * }
     * @param serviceName service name
     * @return json
     */
    @GetMapping(path = "/service/{serviceName}/interface/downstreamInterfaces", consumes = "application/json")
    public JSONObject getServiceInterfaceDownstreamInterfaces(@PathVariable(name = "serviceName") String serviceName,
                                                              @RequestBody String body){
        JSONObject ret = new JSONObject();
        ret.put("interfaces", new JSONArray());
        Map<String, Map<String, Tuple2<Interface, LinkedList<Tuple3<Long, Long, Long>>>>> map = this.service.getDownstreamInterfaceMap(
                serviceName,
                JSON.parseObject(body).getString("interface")
        );
        if(map != null){
            for(String downstreamService : map.keySet()){
                for(String downstreamInterface : map.get(downstreamService).keySet()){
                    JSONObject obj = new JSONObject();
                    obj.put("name", downstreamService + ":" + downstreamInterface);
                    obj.put("respTime", new JSONArray());
                    for(Tuple3<Long, Long, Long> time : map.get(downstreamService).get(downstreamInterface).second){
                        JSONObject _obj = new JSONObject();
                        _obj.put("startUnixTimeNano", time.first);
                        _obj.put("endUnixTimeNano", time.second);
                        _obj.put("responseTimeNano", time.third);
                        obj.getJSONArray("respTime").add(_obj);
                    }
                    ret.getJSONArray("interfaces").add(obj);
                }
            }
        }
        return ret;
    }

    /**
     * response
     * {
     *      "name": "serviceName:interfaceName",
     *      "respTime": [
     *          {
     *              "startUnixTimeNano": 1234,
     *              "endUnixTimeNano": 1235,
     *              "responseTimeNano": 1
     *          },
     *          ...
     *      ]
     * }
     * request
     * {
     *     "interface": "xxx",
     *     "downstreamService": "xxx",
     *     "downstreamInterface": "xxx"
     * }
     * @param serviceName service name
     * @param body interface name and downstream interface name
     * @return json
     */
    @GetMapping(path = "/service/{serviceName}/interface/downstreamInterface", consumes = "application/json")
    public JSONObject getServiceInterfaceDownstreamInterface(@PathVariable(name = "serviceName") String serviceName,
                                                             @RequestBody String body){
        JSONObject requestBodyJSON = JSON.parseObject(body);
        String interfaceName = requestBodyJSON.getString("interface");
        String downstreamInterfaceName = requestBodyJSON.getString("downstreamInterface");
        String downstreamServiceName = requestBodyJSON.getString("downstreamService");

        JSONObject ret = new JSONObject();
        ret.put("name", downstreamServiceName + ":" + downstreamInterfaceName);
        ret.put("respTime", new JSONArray());

        Service srv = this.service.getServiceMap().getOrDefault(serviceName, null);
        if(srv != null){
            Interface i = srv.getInterface(interfaceName);
            if(i != null){
                Map<String, Tuple2<Interface, LinkedList<Tuple3<Long, Long, Long>>>> dS = i.getDownstreamInterfaceMap().getOrDefault(downstreamServiceName, null);
                if(dS != null){
                    Tuple2<Interface, LinkedList<Tuple3<Long, Long, Long>>> di = dS.getOrDefault(downstreamInterfaceName, null);
                    if(di != null){
                        for(Tuple3<Long, Long, Long> time : di.second){
                            JSONObject obj = new JSONObject();
                            obj.put("startUnixTimeNano", time.first);
                            obj.put("endUnixTimeNano", time.second);
                            obj.put("responseTimeNano", time.third);
                            ret.getJSONArray("respTime").add(obj);
                        }
                    }
                }
            }
        }

        return ret;
    }
}
