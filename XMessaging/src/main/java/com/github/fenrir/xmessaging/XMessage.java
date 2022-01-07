package com.github.fenrir.xmessaging;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.fenrir.xmessaging.exceptions.NoSuchTypeHeaderException;
import io.nats.client.Message;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class XMessage {

    public enum RawType {
        NATS
    }

    private Message natsRawMessage;
    private CompletableFuture<Message> natsRawFutureMessage;
    private RawType rawType;

    private XMessage(Message rawMessage) {
        this.natsRawMessage = rawMessage;
        this.rawType = RawType.NATS;
    }

    public XMessage(CompletableFuture<Message> rawFutureMessage){
        this.natsRawFutureMessage = rawFutureMessage;
    }

    @SuppressWarnings("unchecked")
    public <T> T getHeader(String key, Class<?> type)
            throws NoSuchTypeHeaderException {
        switch (this.rawType) {
            case NATS:
                if(type == String.class || type == List.class){
                    if(type == String.class)
                        return (T) natsRawMessage.getHeaders().get(key).get(0);
                    else return (T) natsRawMessage.getHeaders().get(key);
                }else throw new NoSuchTypeHeaderException();
            default:
                return null;
        }
    }

    public Message getNatsRawMessage(){
        return this.natsRawMessage;
    }

    public String getStringData(){
        if(this.rawType == RawType.NATS)
            return new String(this.natsRawMessage.getData(), StandardCharsets.UTF_8);
        else return null;
    }

    public JSONObject getJSONObjectFromData(){
        if(this.rawType == RawType.NATS)
            return JSON.parseObject(new String(this.natsRawMessage.getData(), StandardCharsets.UTF_8));
        else return null;
    }

    public static XMessage create(Message msg){
        return new XMessage(msg);
    }
}
