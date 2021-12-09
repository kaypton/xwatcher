package com.github.fenrir.xlocalmonitor.entities;

import com.github.fenrir.xlocalmonitor.entities.exceptions.*;

import javax.ws.rs.core.Link;
import java.util.*;

public class MessageBuilder {

    private final String kind;
    private final String name;
    private MessageEntity entity;

    private LinkedList<Map<String, String>> objEntityContainerStack = new LinkedList<>();
    private Object objEntityContainer;

    private LinkedList<Map<String, Object>> objContainerStack = new LinkedList<>();
    private Map<String, Object> objContainer;


    private MessageBuilder(String kind, String name) throws UnknownKindException, NoSuchMessageException {
        this.kind = kind;
        this.name = name;

        if(kind.equals("Stream") || kind.equals("Event")){
            this.entity = MessageEntityFactory.getMessageEntity(name, kind);
            if(this.entity == null) throw new NoSuchMessageException();
            this.objEntityContainer = this.entity.data;
            this.objContainer = new LinkedHashMap<>();
            this.objContainerStack.addLast(this.objContainer);
        } else {
            throw new UnknownKindException();
        }
    }

    @SuppressWarnings("unchecked")
    public <T> MessageBuilder withValue(String key, T value, Class<T> valueType) throws NotFoundKeyException,
            WrongDataTypeException, NoSuchDataTypeException, MessageBuilderRuntimeException {
        if(this.objEntityContainer == null)
            throw new MessageBuilderRuntimeException();
        if(((Map<String, String>) this.objEntityContainer).containsKey(key)){
            boolean ok = this.checkType(((Map<String, String>) this.objEntityContainer).get(key), valueType);
            if(ok){
                if(this.objContainer == null)
                    throw new MessageBuilderRuntimeException();
                this.objContainer.put(key, value);
            }else{
                throw new WrongDataTypeException();
            }
        }else{
            throw new NotFoundKeyException();
        }
        return this;
    }

    @SuppressWarnings("unchecked")
    public MessageBuilder withObject(String key) throws NotFoundKeyException, NotObjectException,
            NoSuchDataDefException {
        if(((Map<String, String>) this.objEntityContainer).containsKey(key)){
            String type = ((Map<String, String>) this.objEntityContainer).get(key);
            if(type.charAt(0) != '$') throw new NotObjectException();
            else {
                if(((Map<String, String>) this.objEntityContainer).containsKey("buildingName")){
                    ((Map<String, String>) this.objEntityContainer).replace("buildingName", key);
                }else{
                    ((Map<String, String>) this.objEntityContainer).put("buildingName", key);
                }
                Map<String, String> dataDef = this.findDataDef(type.substring(1));
                if(dataDef == null) throw new NoSuchDataDefException();
                this.objEntityContainerStack.addLast(dataDef);
                this.objEntityContainer = dataDef;
                this.objContainer = new LinkedHashMap<>();
                this.objContainerStack.addLast(this.objContainer);
            }
        }else{
            throw new NotFoundKeyException();
        }
        return this;
    }

    @SuppressWarnings("unchecked")
    public MessageBuilder buildObject() throws MessageBuilderRuntimeException {
        if(this.objEntityContainerStack.size() == 0)
            throw new MessageBuilderRuntimeException();
        this.objEntityContainerStack.removeLast();
        if(this.objEntityContainerStack.size() == 0)
            this.objEntityContainer = this.entity.data;
        else this.objEntityContainer = this.objEntityContainerStack.getLast();
        String buildingName = ((Map<String, String>) this.objEntityContainer).get("buildingName");
        Map<String, Object> tmpObjContainer = this.objContainer;
        this.objContainerStack.removeLast();
        this.objContainer = this.objContainerStack.getLast();
        this.objContainer.put(buildingName, tmpObjContainer);
        ((Map<String, String>) this.objEntityContainer).remove("buildingName");
        return this;
    }

    private Map<String, String> findDataDef(String name){
        if(this.entity.dataDefs != null)
            for(Map<String, String> dataDef : this.entity.dataDefs){
                assert dataDef.containsKey("name");
                if(dataDef.get("name").equals(name)){
                    return dataDef;
                }
            }

        return MessageEntityFactory.getGlobalDataDef(name);
    }

    private boolean checkType(String typeStr, Class<?> type) throws NoSuchDataTypeException {
        switch (typeStr) {
            case "string":
                return type == String.class;
            case "double":
                return type == Double.class;
            case "int":
                return type == Integer.class;
            case "long":
                return type == Long.class;
            case "map":
                return type == Map.class || type == HashMap.class || type == LinkedHashMap.class;
            case "list":
                return type == List.class;
            default:
                throw new NoSuchDataTypeException();
        }
    }

    public Map<String, Object> build() throws MessageBuilderRuntimeException {
        if(this.objContainerStack.size() != 1){
            throw new MessageBuilderRuntimeException();
        }else return this.objContainerStack.getLast();
    }

    public static MessageBuilder builder(String kind, String name) throws UnknownKindException, NoSuchMessageException {
        return new MessageBuilder(kind, name);
    }
}
