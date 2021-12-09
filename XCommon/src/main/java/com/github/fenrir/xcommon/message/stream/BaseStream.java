package com.github.fenrir.xcommon.message.stream;

import com.alibaba.fastjson.JSON;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class BaseStream {
    private static final Map<String, Class<? extends BaseStream>> streamMap =
            new ConcurrentHashMap<>();

    public String streamTopicName;
    public String streamName;
    public String streamDescription;

    protected abstract Object setStreamData(Object... params);

    public Map<String, String> createStreamUnit(String uuid, Object... params){
        Map<String, String> map = new HashMap<>();
        map.put("key", uuid);
        map.put("value", JSON.toJSONString(setStreamData(params)));
        return map;
    }

    public void setStreamName(String streamName){
        this.streamName = streamName;
    }

    public void setStreamTopicName(String streamTopicName){
        this.streamTopicName = streamTopicName;
    }

    public void setStreamDescription(String streamDescription){
        this.streamDescription = streamDescription;
    }

    public String getStreamName(){
        return this.streamName;
    }
    public String getStreamTopicName(){
        return this.streamTopicName;
    }
    public String getStreamDescription(){
        return this.streamDescription;
    }
}
