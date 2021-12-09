package com.github.fenrir.xmessaging;

import io.nats.client.Message;
import io.nats.client.impl.Headers;
import io.nats.client.impl.NatsMessage;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class XMessageBuilder {
    private Map<String, String[]> lheaders = null;
    private Map<String, Collection<String>> cheaders = null;
    private Map<String, String> headers = null;

    private String topic = null;
    private byte[] payload = null;

    private Map<String, String[]> getLHeaders(){
        if(this.lheaders == null){
            this.lheaders = new HashMap<>();
        }
        return this.lheaders;
    }

    private Map<String, Collection<String>> getCHeaders(){
        if(this.cheaders == null){
            this.cheaders = new HashMap<>();
        }
        return this.cheaders;
    }

    private Map<String, String> getHeaders(){
        if(this.headers == null){
            this.headers = new HashMap<>();
        }
        return this.headers;
    }

    public XMessageBuilder addHeader(String key, String value){
        this.getHeaders().put(key, value);
        return this;
    }

    public XMessageBuilder addHeader(String key, String... value){
        this.getLHeaders().put(key, value);
        return this;
    }

    public XMessageBuilder addHeader(String key, Collection<String> value){
        this.getCHeaders().put(key, value);
        return this;
    }

    public XMessageBuilder setStringPayload(String data){
        this.payload = data.getBytes(StandardCharsets.UTF_8);
        return this;
    }

    public XMessageBuilder setPayload(byte[] data){
        this.payload = data;
        return this;
    }

    private void setTopic(String topic){
        this.topic = topic;
    }

    public XMessage buildNatsMessage(){
        Headers headers = new Headers();
        for(String key : this.getHeaders().keySet()){
            headers.add(key, this.getHeaders().get(key));
        }

        for(String key : this.getCHeaders().keySet()){
            headers.add(key, this.getCHeaders().get(key));
        }

        for(String key : this.getLHeaders().keySet()){
            headers.add(key, this.getLHeaders().get(key));
        }

        Message msg = NatsMessage.builder()
                .subject(this.topic)
                .data(this.payload)
                .headers(headers)
                .build();

        return new XMessage(msg);
    }

    static public XMessageBuilder builder(String topic){
        XMessageBuilder builder = new XMessageBuilder();
        builder.setTopic(topic);
        return builder;
    }
}
