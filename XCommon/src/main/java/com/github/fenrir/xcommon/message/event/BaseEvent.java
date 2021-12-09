package com.github.fenrir.xcommon.message.event;

import com.alibaba.fastjson.JSON;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Ace <br/>
 * class {@code Event} <br/>
 * 所有具体的事件定义都要继承于 {@code Event} 类
 */
public abstract class BaseEvent {
    private static final Map<String, Class<? extends BaseEvent>> eventMap = new HashMap<>();

    private String eventName;
    private String eventTopicName;
    private String eventDescription;

    public abstract Object setExtraData(Object... params);

    public Map<String, String> createEvent(String uuid, Object... params){
        Map<String, String> map = new HashMap<>();
        map.put("key", uuid);
        map.put("value", JSON.toJSONString(setExtraData(params)));
        return map;
    }

    public void setEventName(String eventName){
        this.eventName = eventName;
    }

    public void setEventTopicName(String eventTopicName){
        this.eventTopicName = eventTopicName;
    }

    public void setEventDescription(String eventDescription){
        this.eventDescription = eventDescription;
    }

    public String getEventName(){
        return this.eventName;
    }
    public String getEventTopicName(){
        return this.eventTopicName;
    }
    public String getEventDescription(){
        return this.eventDescription;
    }
}
