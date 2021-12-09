package com.github.fenrir.xlocalmonitor.inspectors.thirdpart.clients.netdataclient.metrics;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.annotation.JSONField;
import lombok.Getter;
import lombok.Setter;

public class NetdataBaseMetric {
    @JSONField(name="labels")
    @Getter @Setter public String[] labels;

    @JSONField(name="data")
    @Getter @Setter public JSONArray[] data;

    public Integer getMetricNum(){
        return data.length;
    }

    protected Integer getLabelIndex(String labelName){
        for (int i = 0; i < labels.length; i++) {
            if (this.getLabels()[i].equals(labelName)) {
                return i;
            }
        }
        return -1;
    }

    public <T> Value<T> getLatestMetric(Object kind, Class<T> dataType){
        return getLatestMetricByName(kind.toString(), dataType);
    }

    public <T> Value<T> getLatestMetricByName(String name, Class<T> dataType){
        if(this.getMetricNum() == 0) return null;
        else{
            int index = this.getLabelIndex(name);
            if(index == -1) return null;
            return new Value<>(
                    this.getData()[0].getObject(index, dataType),
                    this.getData()[0].getLong(
                            this.getLabelIndex("time")
                    )
            );
        }
    }

    public <T> Value<T> getEarliestMetric(Object kind, Class<T> dataType){
        return getEarliestMetricByName(kind.toString(), dataType);
    }

    public <T> Value<T> getEarliestMetricByName(String name, Class<T> dataType){
        if(this.getMetricNum() == 0) return null;
        else{
            int index = this.getLabelIndex(name);
            if(index == -1) return null;
            return new Value<>(
                    this.getData()[this.getData().length - 1].getObject(index, dataType),
                    this.getData()[this.getData().length - 1].getLong(
                            this.getLabelIndex("time")
                    )
            );
        }
    }
}
