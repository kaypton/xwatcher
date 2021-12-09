package com.github.fenrir.xlocalmonitor.inspectors.thirdpart.clients.netdataclient;

import com.alibaba.fastjson.JSONObject;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class NetdataChartsDescription {
    @Getter @Setter private JSONObject descriptionJSON = null;

    private static class Dimension {
        @Getter @Setter private String dimensionName = null;
        @Getter @Setter private String dimensionJSONKeyName = null;
    }

    public NetdataChartsDescription(JSONObject descriptionJSON){
        this.setDescriptionJSON(descriptionJSON);
    }

    public String getHostname(){
        return this.getDescriptionJSON().getString("hostname");
    }

    public Integer getUpdateEvery(){
        return this.getDescriptionJSON().getInteger("update_every");
    }

    public String getChartId(String chartName){
        JSONObject chart = this.getDescriptionJSON().getJSONObject("charts")
                .getJSONObject(chartName);
        if(chart != null){
            return chart.getString("id");
        }else return null;
    }

    public String getChartUnits(String chartName){
        JSONObject chart = this.getDescriptionJSON().getJSONObject("charts")
                .getJSONObject(chartName);
        if(chart != null){
            return chart.getString("units");
        }else return null;
    }

    public List<Dimension> getChartDimensions(String chartName){
        JSONObject chart = this.getDescriptionJSON().getJSONObject("charts")
                .getJSONObject(chartName);
        List<Dimension> ret = new ArrayList<>();
        if(chart != null){
            JSONObject dimensions = chart.getJSONObject("dimensions");
            if(dimensions != null){
                Set<String> keySet = dimensions.keySet();
                if(keySet.size() == 0) return null;
                else{
                    for(String key : keySet){
                        JSONObject dimension = dimensions.getJSONObject(key);
                        Dimension dimension1 = new Dimension();
                        dimension1.setDimensionJSONKeyName(key);
                        dimension1.setDimensionName(dimension.getString("name"));
                        ret.add(dimension1);
                    }
                    return ret;
                }
            }else return null;
        }else return null;
    }

    public String getChartDataURL(String chartName){
        JSONObject chart = this.getDescriptionJSON().getJSONObject("charts")
                .getJSONObject(chartName);
        if(chart != null){
            return chart.getString("data_url");
        }else return null;
    }
}
