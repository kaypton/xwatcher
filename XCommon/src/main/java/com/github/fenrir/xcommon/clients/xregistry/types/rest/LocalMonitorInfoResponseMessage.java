package com.github.fenrir.xcommon.clients.xregistry.types.rest;

import com.github.fenrir.xcommon.clients.xregistry.entities.mongo.LocalMonitor;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

@ApiModel(
        description = "Response message with LocalMonitor Information",
        parent = BaseResponseMessage.class
)
public class LocalMonitorInfoResponseMessage extends BaseResponseMessage {

    @ApiModelProperty(
        allowEmptyValue = true
    )
    private List<LocalMonitor> data;

    public LocalMonitorInfoResponseMessage(int status, String msg, List<LocalMonitor> data) {
        super(status, msg);
        this.data = data;
    }

    public List<LocalMonitor> getData() {
        return data;
    }

    public void setData(List<LocalMonitor> data) {
        this.data = data;
    }
}
