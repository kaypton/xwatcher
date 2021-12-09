package com.github.fenrir.xlocalmonitor.services.pipeline;

import com.alibaba.fastjson.JSONObject;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * data JSONObject example:
 * {
 *     "metric_type": "event or stream",
 *     "metric_key": one key value,
 *     "metric_value": metric value
 * }
 */
@Service
public class PipelineContainer {
    // from --> to --> pipeline instance
    @Getter @Setter private Map<String, Map<String, Pipeline>> pipelineMap;

    public PipelineContainer(){
        this.setPipelineMap(new ConcurrentHashMap<>());
    }

    public JSONObject getMetricFromPipe(String from, String to){
        try{
            Map<String, Pipeline> toMap = this.getPipelineMap().get(from);
            Pipeline pipeline = toMap.get(to);
            if(pipeline == null) return null;
            else{
                return pipeline.getMetric();
            }
        } catch (NullPointerException e){
            return null;
        }
    }

    public void putMetricToPipe(String from, JSONObject jsonObject){
        try{
            Map<String, Pipeline> toMap = this.getPipelineMap().get(from);
            for(String to : toMap.keySet()){
                toMap.get(to).putMetric(jsonObject);
            }
        } catch (NullPointerException ignored){
        }
        /*Map<String, Pipeline> toMap = this.getPipelineMap().get(from);
        if(toMap != null){
            for(String to : toMap.keySet()){
                toMap.get(to).putMetric(jsonObject);
            }
        }*/
    }
}
