package com.github.fenrir.xtraceprocessor.protobuf;

public class TraceTest {
    public static void main(String[] args){
        Trace.Span.newBuilder()
                .setStartTimeUnixNano(123.4)
                .setEndTimeUnixNano(124.5)
                .setInstanceUuid("1234")
                .setInterfaceName("hello")
                .setInterfaceUri("/hello")
                .build();
    }
}
