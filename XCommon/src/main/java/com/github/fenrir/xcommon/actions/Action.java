package com.github.fenrir.xcommon.actions;

import com.github.fenrir.xcommon.actions.virtualmachines.VMLiveMigrateAction;
import lombok.Getter;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * {
 *     "action_name": "xxx",
 *     "options": {
 *         "opt1": xxx
 *     }
 * }
 */
public abstract class Action<ACTD> {

    static public final String VM_LIVE_MIGRATE = "vm.live.migrate";

    static public final String CONTAINER_MIGRATE = "container.migrate";

    public ACTD options;

    public Action(ACTD opts){
        this.setOptions(opts);
    }

    public String agentName;

    public abstract String getActionName();
    public abstract void setOptions(ACTD opts);
    public abstract ACTD getOptions();
    public abstract String toJSONString();

    public static Action<Object> createAction(String actionName, Object opts){
        ObjectAction objectAction = new ObjectAction(opts);
        objectAction.setActionName(actionName);
        return objectAction;
    }

    public static <ACTD> Action<ACTD> createAction(ACTD opts,
                                                   Class<? extends Action<ACTD>> actionClass,
                                                   Class<ACTD> optsDataClass){
        try {
            return actionClass.getDeclaredConstructor(optsDataClass).newInstance(opts);
        } catch (NoSuchMethodException | InvocationTargetException |
                InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }
}
