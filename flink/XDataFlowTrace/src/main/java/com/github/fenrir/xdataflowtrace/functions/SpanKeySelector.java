package com.github.fenrir.xdataflowtrace.functions;

import com.github.fenrir.xdataflowtrace.entity.SpanWithCount;
import org.apache.flink.api.java.functions.KeySelector;

public class SpanKeySelector implements KeySelector<SpanWithCount, String> {
    public enum Key {
        SERVICE_NAME,
        INTERFACE_NAME,
        INTERFACE_URI
    }

    private final Key keyBy;
    public SpanKeySelector(Key keyBy){
        this.keyBy = keyBy;
    }
    @Override
    public String getKey(SpanWithCount spanWithCount) throws Exception {
        switch (this.keyBy) {
            case INTERFACE_NAME:
                return spanWithCount.span.getInterfaceName();
            case INTERFACE_URI:
                return spanWithCount.span.getInterfaceURI();
            case SERVICE_NAME:
                return spanWithCount.span.getServiceName();
        }
        throw new Exception("unknown key type");
    }
}
