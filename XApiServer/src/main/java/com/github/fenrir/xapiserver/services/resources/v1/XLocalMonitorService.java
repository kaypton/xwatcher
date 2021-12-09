package com.github.fenrir.xapiserver.services.resources.v1;

import com.github.fenrir.xcommon.clients.xapiserver.api.rest.responseEntities.api.v1.XLocalMonitorDeleteResponse;
import com.github.fenrir.xcommon.clients.xapiserver.api.rest.responseEntities.api.v1.XLocalMonitorGetResponse;
import com.github.fenrir.xcommon.clients.xapiserver.api.rest.responseEntities.api.v1.XLocalMonitorUpdateResponse;

public interface XLocalMonitorService {
    XLocalMonitorUpdateResponse update(String hostname, String ipAddress);
    XLocalMonitorGetResponse get(String hostname, String ipAddress);
    XLocalMonitorGetResponse getAll();
    XLocalMonitorDeleteResponse delete(String hostname, String ipAddress);
}
