package com.github.fenrir.xplanner.objects;

import com.github.fenrir.xcommon.actions.Action;
import com.github.fenrir.xcommon.utils.CommonUtils;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Plan {
    // UUID string -> Action
    @Getter @Setter private String uuid;
    @Getter @Setter private String agent;
    @Getter private final Map<String, Action<Object>> actionList =
            new ConcurrentHashMap<>();

    public Plan(String uuid, String agent){
        this.setUuid(uuid);
        this.setAgent(agent);
    }

    public void addAction(Action<Object> action){
        String uuid = CommonUtils.getUnusedUUID(this.getActionList().keySet());
        actionList.put(uuid, action);
    }
}
