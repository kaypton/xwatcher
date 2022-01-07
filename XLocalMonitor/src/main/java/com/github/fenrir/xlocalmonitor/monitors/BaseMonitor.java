package com.github.fenrir.xlocalmonitor.monitors;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.fenrir.xcommon.utils.Tuple2;
import com.github.fenrir.xlocalmonitor.entities.MessageEntity;
import com.github.fenrir.xlocalmonitor.entities.MessageEntityFactory;
import com.github.fenrir.xlocalmonitor.services.pipeline.PipelineContainer;
import com.github.fenrir.xmessaging.XMessaging;
import com.github.fenrir.xmessaging.XMessagingPublisher;
import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public abstract class BaseMonitor implements Runnable {

    @Getter protected Map<String, Object> apiMap;

    @Getter protected String uuid;
    @Getter protected String hostname;

    private final Map<String, Tuple2<XMessagingPublisher, XMessagingPublisher>> streamMap = new HashMap<>();
    private final Map<String, Tuple2<XMessagingPublisher, XMessagingPublisher>> eventMap = new HashMap<>();

    private final Collection<Timer> timerCollection = new HashSet<>();

    @Getter @Setter private PipelineContainer pipelineContainer = null;

    private ThreadPoolExecutor dataPushingExecutor =
            new ThreadPoolExecutor(10, 100, 3, TimeUnit.SECONDS, new LinkedBlockingDeque<>());

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

        this.streamMap.put(streamName, new Tuple2<>(
                XMessaging.createPublisher(streamEntity.topicName),
                XMessaging.createPublisher(uuid + "." + streamEntity.topicName)
        ));
    }

    public void registerEvent(String eventName){
        MessageEntity eventEntity = MessageEntityFactory.getMessageEntity(eventName, "Event");

        if(eventEntity == null) return;

        this.eventMap.put(eventName, new Tuple2<>(
                XMessaging.createPublisher(eventEntity.topicName),
                XMessaging.createPublisher(uuid + "." + eventEntity.topicName)
        ));
    }

    private Map<String, String> createData(Map<String, Object> map){
        Map<String, String> ret = new HashMap<>();
        ret.put("key", this.getUuid());
        ret.put("value", JSON.toJSONString(map));
        return ret;
    }

    protected void sendStreamData(String streamName, Map<String, Object> map){
        this.pushData("stream", streamName, this.createData(map), true);
    }

    protected void sendEventData(String eventName, Map<String, Object> map){
        this.pushData("event", eventName, this.createData(map), true);
    }

    protected void pushStreamData(String streamName, Map<String, String> data){
        this.pushData("stream", streamName, data, false);
    }

    protected void pushEventData(String eventName, Map<String, String> data){
        this.pushData("event", eventName, data, false);
    }

    private void pushData(String type,
                          String name,
                          Map<String, String> data,
                          boolean pushToPipeline){
        this.dataPushingExecutor.submit(()->{
           this._pushData(type, name, data, pushToPipeline);
        });
    }

    /**
     * private method, publish a metric by XMessagingStringStreamPublisher<br>
     * <br>
     * @param type stream or event
     * @param name stream or event name
     * @param data a map container "key" and "value"
     */
    private void _pushData(String type,
                          String name,
                          Map<String, String> data,
                          boolean pushToPipeline){
        XMessagingPublisher publisher1;
        XMessagingPublisher publisher2;

        if(type.equals("stream")){
            publisher1 = streamMap.get(name).first;
            publisher2 = streamMap.get(name).second;
        }else if(type.equals("event")){
            publisher1 = eventMap.get(name).first;
            publisher2 = eventMap.get(name).second;
        }else{
            return;
        }

        if(pushToPipeline){
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
        } else{
            publisher1.send(JSON.toJSONString(data));
            publisher2.send(JSON.toJSONString(data));
        }
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
