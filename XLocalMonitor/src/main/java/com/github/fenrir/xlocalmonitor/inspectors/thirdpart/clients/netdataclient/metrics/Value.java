package com.github.fenrir.xlocalmonitor.inspectors.thirdpart.clients.netdataclient.metrics;

import lombok.Getter;
import lombok.Setter;

public class Value <T> {
    @Getter @Setter private T value;
    @Getter @Setter private Long timestamp;

    public Value(T value, Long timestamp){
        this.setValue(value);
        this.setTimestamp(timestamp);
    }
}
