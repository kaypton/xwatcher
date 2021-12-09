package com.github.fenrir.xstrategy.restapis;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.fenrir.xcommon.actions.Action;
import com.github.fenrir.xcommon.clients.xplanner.api.rest.entities.AddActionRequest;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class XPlannerRestAPI {
    @Getter @Setter private static String host;
    @Getter @Setter private String rootURL = "/xplanner";

    public RestTemplate getRestTemplate(){
        return new RestTemplate();
    }

    /**
     * create a new plan, return the plan UUID in XPlanner<br/>
     * return:<br/>
     * {
     *     "uuid": "xxx-xxx-xxxxxxx"
     * }
     * @return json
     */
    public String createNewPlan(String agent){
        String URL = "http://" + host + rootURL + "/create/" + agent;
        return this.getRestTemplate().getForObject(
                URL, String.class
        );
    }

    /**
     * add an action to a plan, return the status<br/>
     * return: <br/>
     * {
     *     "status": "success or error"
     * }
     * @param planUUID plan uuid
     * @param action action
     * @return json
     */
    public String addActionToPlan(String planUUID, Action<?> action){
        String URL = "http://" + host + rootURL + "/add_action";

        AddActionRequest request = new AddActionRequest();
        request.actionName = action.getActionName();
        request.actionOpts = action.getOptions();
        request.planUUID = planUUID;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(
                MediaType.parseMediaType(
                        "application/json; charset=UTF-8"
                )
        );
        headers.add("Accept", MediaType.APPLICATION_JSON.toString());

        return this.getRestTemplate().postForObject(
                URL, new HttpEntity<>(JSON.toJSONString(request), headers),
                String.class
        );
    }

    /**
     * execute a plan
     * @param planUUID plan uuid
     * @return TODO
     */
    public String executePlan(String planUUID){
        String URL = "http://" + host + rootURL + "/execute_plan" +
                "?planUuid=" + planUUID;
        return this.getRestTemplate().getForObject(
                URL, String.class
        );
    }
}
