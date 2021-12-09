package com.github.fenrir.xlocalmonitor.monitors;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.fenrir.xcommon.message.event.BaseEvent;
import com.github.fenrir.xcommon.message.stream.BaseStream;
import com.github.fenrir.xlocalmonitor.entities.MessageEntity;
import com.github.fenrir.xlocalmonitor.entities.MessageEntityFactory;
import com.github.fenrir.xlocalmonitor.services.monitor.XLocalMonitorFactory;
import com.github.fenrir.xlocalmonitor.services.pipeline.PipelineContainer;
import com.github.fenrir.xlocalmonitor.services.prometheus.DataContainerService;
import com.github.fenrir.xmessaging.XMessaging;
import com.github.fenrir.xmessaging.XMessagingPublisher;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

public abstract class BaseMonitor implements Runnable {

    @Getter protected Map<String, Object> apiMap;

    @Getter protected String uuid;
    @Getter protected String hostname;

    private final Map<String, List<Object>> streamMap = new HashMap<>();
    private final Map<String, List<Object>> eventMap = new HashMap<>();
    private final Collection<Timer> timerCollection = new HashSet<>();

    @Getter @Setter private PipelineContainer pipelineContainer = null;

    public BaseMonitor(){}

    public void setUuid(String uuid){
        this.uuid = uuid;
    }

    public void setHostname(String hostname){
        this.hostname = hostname;
    }

    public void setApiMap(Map<String, Object> apiMap){
        this.apiMap = apiMap;
    }

    protected abstract void postStart();
    protected abstract void doStart();
    protected abstract void preStart();

    public static String getMonitorName() {
        return null;
    }

    public abstract void doStop();


    public void registerStream(String streamName){
        MessageEntity streamEntity = MessageEntityFactory.getMessageEntity(streamName, "Stream");

        if(streamEntity == null) return;

        List<Object> list = new ArrayList<>();
        list.add(streamEntity);
        list.add(XMessaging.createPublisher(streamEntity.topicName));
        list.add(XMessaging.createPublisher(uuid + "." + streamEntity.topicName));

        this.streamMap.put(streamName, list);
    }

    public void registerEvent(String eventName){
        MessageEntity eventEntity = MessageEntityFactory.getMessageEntity(eventName, "Event");

        if(eventEntity == null) return;

        List<Object> list = new ArrayList<>();
        list.add(eventEntity);
        list.add(XMessaging.createPublisher(eventEntity.topicName));
        list.add(XMessaging.createPublisher(uuid + "." + eventEntity.topicName));

        this.eventMap.put(eventName, list);
    }

    protected Map<String, String> createStreamData(String streamName, Object... params){
        List<Object> list = this.streamMap.get(streamName);
        if(list != null){
            BaseStream stream = (BaseStream) list.get(0);
            return stream.createStreamUnit(this.getUuid(), params);
        }else return null;
    }

    protected Map<String, String> createEventData(String eventName, Object... params){
        List<Object> list = this.eventMap.get(eventName);
        if(list != null){
            BaseEvent event = (BaseEvent) list.get(0);
            return event.createEvent(this.getUuid(), params);
        }else return null;
    }

    private Map<String, String> createData(Map<String, Object> map){
        Map<String, String> ret = new HashMap<>();
        ret.put("key", this.getUuid());
        ret.put("value", JSON.toJSONString(map));
        return ret;
    }

    protected void sendStreamData(String streamName, Map<String, Object> map){
        this.pushData("stream", streamName, this.createData(map));
    }

    protected void sendEventData(String eventName, Map<String, Object> map){
        this.pushData("event", eventName, this.createData(map));
    }

    protected void pushStreamData(String streamName, Map<String, String> data){
        this.pushData("stream", streamName, data);
    }

    protected void pushEventData(String eventName, Map<String, String> data){
        this.pushData("event", eventName, data);
    }

    /**
     * private method, publish a metric by XMessagingStringStreamPublisher<br>
     * <br>
     * @param type stream or event
     * @param name stream or event name
     * @param data a map container "key" and "value"
     */
    private void pushData(String type, String name, Map<String, String> data){
        XMessagingPublisher publisher1;
        XMessagingPublisher publisher2;

        if(type.equals("stream")){
            publisher1 = (XMessagingPublisher) streamMap.get(name).get(1);
            publisher2 = (XMessagingPublisher) streamMap.get(name).get(2);
        }else if(type.equals("event")){
            publisher1 = (XMessagingPublisher) eventMap.get(name).get(1);
            publisher2 = (XMessagingPublisher) eventMap.get(name).get(2);
        }else{
            return;
        }

        /* push the data to the pipeline */
        JSONObject pipelineData = new JSONObject();
        pipelineData.put("metric_type", type);
        pipelineData.put("metric_key", data.get("key"));
        pipelineData.put("metric_value", data.get("value"));

        this.getPipelineContainer().putMetricToPipe(
                getMonitorName(), pipelineData
        );

        publisher1.send(data.get("value"));
        publisher2.send(data.get("value"));
    }

    protected JSONObject getMetricFromPipe(String from){
        if(this.getPipelineContainer() == null) return null;
        else return this.getPipelineContainer().getMetricFromPipe(
                from, getMonitorName()
        );
    }

    protected void registerTimerTask(TimerTask task, Long period){
        Timer timer = new Timer();
        timer.schedule(task, 0, period);
        this.timerCollection.add(timer);
    }

    abstract public Map<String, Map<String, Object>> extract();

    @Override
    public void run(){
        this.preStart();
        this.doStart();
        this.postStart();
    }
}
