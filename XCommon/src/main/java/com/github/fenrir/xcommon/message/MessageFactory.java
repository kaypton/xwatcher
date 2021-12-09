package com.github.fenrir.xcommon.message;

import com.github.fenrir.xcommon.message.annotations.Event;
import com.github.fenrir.xcommon.message.annotations.Stream;
import com.github.fenrir.xcommon.message.event.BaseEvent;
import com.github.fenrir.xcommon.message.stream.BaseStream;
import org.reflections.Reflections;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class MessageFactory {

    private final Map<String, BaseStream> streamMap = new ConcurrentHashMap<>();
    private final Map<String, BaseEvent> eventMap = new ConcurrentHashMap<>();

    public MessageFactory(String[] msgDefinitionPath){
        for(String path : msgDefinitionPath){
            Reflections reflections = new Reflections(path);
            Set<Class<?>> eventClazz = reflections.getTypesAnnotatedWith(Event.class);
            for(Class<?> eventClass : eventClazz){
                Event eventAnnotation = eventClass.getDeclaredAnnotation(Event.class);
                String eventName = eventAnnotation.name();
                String eventTopicName = eventAnnotation.topicName();
                String eventDescription = eventAnnotation.description();

                try {
                    BaseEvent baseEvent = (BaseEvent) eventClass.getDeclaredConstructor().newInstance();
                    baseEvent.setEventName(eventName);
                    baseEvent.setEventDescription(eventDescription);
                    baseEvent.setEventTopicName(eventTopicName);
                    this.eventMap.put(eventName, baseEvent);
                } catch (InstantiationException
                        | IllegalAccessException
                        | InvocationTargetException
                        | NoSuchMethodException e) {
                    e.printStackTrace();
                }
            }

            Set<Class<?>> streamClazz = reflections.getTypesAnnotatedWith(Stream.class);
            for(Class<?> streamClass : streamClazz){
                Stream streamAnnotation = streamClass.getDeclaredAnnotation(Stream.class);
                String streamName = streamAnnotation.name();
                String streamTopicName = streamAnnotation.topicName();
                String streamDescription = streamAnnotation.description();

                try {
                    BaseStream baseStream = (BaseStream) streamClass.getDeclaredConstructor().newInstance();
                    baseStream.setStreamName(streamName);
                    baseStream.setStreamDescription(streamDescription);
                    baseStream.setStreamTopicName(streamTopicName);
                    this.streamMap.put(streamName, baseStream);
                } catch (InstantiationException
                        | IllegalAccessException
                        | InvocationTargetException
                        | NoSuchMethodException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    static public MessageFactory create(String[] msgDefinitionPath){
        return new MessageFactory(msgDefinitionPath);
    }

    public BaseEvent getEvent(String eventName){
        return this.eventMap.getOrDefault(eventName, null);
    }

    public BaseStream getStream(String streamName){
        return this.streamMap.getOrDefault(streamName, null);
    }
}
