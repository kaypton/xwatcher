package com.github.fenrir.xopenstackagent.services.executors;

import com.github.fenrir.xcommon.actions.Action;
import org.openstack4j.model.common.ActionResponse;

public interface ActionExecutor {
    ActionResponse doAction(Action<Object> payload);
}
