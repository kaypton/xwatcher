package com.github.fenrir.xlocalmonitor.entities;

import com.github.fenrir.xcommon.utils.CommonUtils;
import com.github.fenrir.xlocalmonitor.entities.exceptions.*;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MessageEntityFactory {
    /* Kinds: Stream Event DataDef */

    /**
     * the `messageEntity' is Map<{Kind}, Map<{Name}, MessageEntity>>
     */
    private static final Map<String, Map<String, MessageEntity>> messageEntityMap = new HashMap<>();
    /**
     * the `globalDataDefs' is Map<{DataDefName}, Map<{key}, {value}>>
     */
    private static final Map<String, Map<String, String>> globalDataDefs = new HashMap<>();

    public static void init(String messageDefinitionPath)
        throws NoFilesFoundException, UnknownFileTypeException, NoYamlObjectException, UnknownKindException,
            DuplicateDataDefException, NoNameSpecifiedException, NoTopicNameSpecifiedException, NoDataException,
            NoKindSpecifiedException{

        messageEntityMap.put("Event", new ConcurrentHashMap<>());
        messageEntityMap.put("Stream", new ConcurrentHashMap<>());

        // ! Do not support recursion reading !
        File file = new File(messageDefinitionPath);
        if(file.isFile()){ // the path is a file
            Yaml yaml = new Yaml();
            Iterable<Object> data = null;
            try {
                data = yaml.loadAll(new FileInputStream(file));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            if(data == null){
                throw new NoYamlObjectException();
            }
            for(Object obj : data){
                loadMessageEntity(obj);
            }
        }else if(file.isDirectory()){ // the path is a dir
            File[] files = file.listFiles();
            if(files != null)
                for(File _file : files){
                    Yaml yaml = new Yaml();
                    Iterable<Object> data = null;
                    try {
                        data = yaml.loadAll(new FileInputStream(_file));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    if(data == null){
                        throw new NoYamlObjectException();
                    }
                    for(Object obj : data){
                        loadMessageEntity(obj);
                    }
                }
            else{ // no file under the dir
                throw new NoFilesFoundException();
            }
        }else{
            throw new UnknownFileTypeException();
        }
    }

    public static MessageEntity getMessageEntity(String name, String kind){
        if(messageEntityMap.containsKey(kind)){
            return messageEntityMap.get(kind).getOrDefault(name, null);
        }else{
            return null;
        }
    }

    public static Map<String, String> getGlobalDataDef(String name){
        return globalDataDefs.getOrDefault(name, null);
    }

    /**
     * Load one yaml object.
     * It will be a Stream, Event or Data Definition (DataDef)
     * @param obj The yaml object, in snakeyaml, it is a `LinkedHashMap' instance
     * @throws UnknownKindException The `Kind' value is neither `DataDef', `Event' or `Stream'
     * @throws DuplicateDataDefException There are more than one entity has the same `Kind' and `Name'
     * @throws NoNameSpecifiedException No name specified in the yaml
     * @throws NoTopicNameSpecifiedException No topic name specified in the yaml
     * @throws NoDataException No data specified in the yaml
     * @throws NoKindSpecifiedException No kind specified in the yaml
     */
    @SuppressWarnings("unchecked")
    public static void loadMessageEntity(Object obj) throws UnknownKindException, DuplicateDataDefException,
            NoNameSpecifiedException, NoTopicNameSpecifiedException, NoDataException, NoKindSpecifiedException {
        LinkedHashMap<String, Object> ymlObj = (LinkedHashMap<String, Object>) obj;
        String kind = (String) ymlObj.getOrDefault("Kind", null);
        if(kind == null)
            throw new NoKindSpecifiedException();
        switch (kind) {
            case "DataDef":
                List<LinkedHashMap<String, String>> dataDefs =
                        (List<LinkedHashMap<String, String>>) ymlObj.getOrDefault("DataDefs", null);
                if (dataDefs != null)
                    for (LinkedHashMap<String, String> dataDef : dataDefs) {
                        if (!globalDataDefs.containsKey(dataDef.get("name"))) {
                            globalDataDefs.put(dataDef.get("name"), dataDef);
                        } else {
                            throw new DuplicateDataDefException();
                        }
                    }
                break;
            case "Stream":
            case "Event":
                MessageEntity messageEntity = new MessageEntity();
                messageEntity.kind = kind;
                messageEntity.name = (String) ymlObj.getOrDefault("Name", null);
                if(messageEntity.name == null)
                    throw new NoNameSpecifiedException();
                messageEntity.topicName = (String) ymlObj.getOrDefault("TopicName", null);
                if(messageEntity.topicName == null)
                    throw new NoTopicNameSpecifiedException();
                messageEntity.data = (Map<String, String>) ymlObj.getOrDefault("Data", null);
                if(messageEntity.data == null)
                    throw new NoDataException();
                messageEntity.dataDefs = (List<Map<String, String>>) ymlObj.getOrDefault("DataDefs", null);
                messageEntityMap.get(kind).put(messageEntity.name, messageEntity);
                break;
            default:   // unknown `Kind'
                throw new UnknownKindException();
        }
    }

    public static void main(String[] args){
        try {
            MessageEntityFactory.init("/Users/cnono/xwatcher/XLocalMonitor/src/messages");

            Map<String, Object> result = MessageBuilder.builder("Stream", "system.ram")
                    .withValue("timestamp", (long) 123, Long.class)
                    .withObject("hostInfo")  /* HostInfo */
                    .withValue("host", "node1", String.class)
                    .withValue("monitorId", "111-111-111", String.class)
                    .buildObject()           /* end HostInfo */
                    .withObject("value")      /* SystemRamStat */
                    .withValue("mem.total.KiB", (long) 1000, Long.class)
                    .withValue("mem.free.KiB", (long) 1000, Long.class)
                    .withValue("mem.available.KiB", (long) 1000, Long.class)
                    .buildObject()           /* end SystemRamStat */
                    .build();
            System.out.println(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
