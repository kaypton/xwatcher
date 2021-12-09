package com.github.fenrir.xlocalmonitor.entities;

import java.util.List;
import java.util.Map;

public class MessageEntity {
    public String kind;
    public String name;
    public String topicName;
    public Map<String, String> data;
    public List<Map<String, String>> dataDefs;
}
