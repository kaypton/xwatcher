package com.github.fenrir.xservicedependency.entities.trace;

import com.alibaba.fastjson.annotation.JSONField;

public class OpenTelemetryTraceData {

    public static class Event {
        public Double timeUnixNano;
        public String name;
    }

    public static class Span {
        public String traceId;
        public String spanId;
        public Double endTimeUnixNano;
        public String kind;
        public String name;
        public Double startTimeUnixNano;
        @JSONField(name = "parentSpanId")
        public String parentSpanId;
        public Event[] events;
    }

    public static class InstrumentationLibrary {
        public String name;
        public String version;
    }

    public static class InstrumentationLibrarySpan {
        public Span[] spans;
        public InstrumentationLibrary instrumentationLibrary;
    }

    public static class ResourceSpan {
        public InstrumentationLibrarySpan[] instrumentationLibrarySpans;
    }

    public ResourceSpan[] resourceSpans;
}
