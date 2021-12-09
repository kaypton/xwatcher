package com.github.fenrir.xlocalmonitor.services.pipeline;

import com.alibaba.fastjson.JSONObject;
import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class Pipeline {
    @Getter @Setter private String from;
    @Getter @Setter private String to;
    @Getter final private BlockingQueue<JSONObject> queue = new LinkedBlockingQueue<>();

    public JSONObject getMetric(){
        try{
            return this.getQueue().poll(10, TimeUnit.SECONDS);
        } catch (InterruptedException e){
            e.printStackTrace();
            return null;
        }
    }

    public void putMetric(JSONObject jsonObject){
        this.getQueue().add(jsonObject);
    }
}
