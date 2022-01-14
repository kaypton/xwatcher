package com.github.fenrir.xdataflowtrace.entity.trace;

public class OpenTelemetryTraceData {

    public static class AttributeValue {
        public String stringValue;
    }

    public static class Attribute {
        public String key;
        public AttributeValue value;
    }

    public static class Event {
        public Double timeUnixNano;
        public String name;
        public Attribute[] attributes;
    }

    public static class Span {
        public String traceId;
        public String spanId;
        public Double endTimeUnixNano;
        public String kind;
        public String name;
        public Double startTimeUnixNano;
        public String parentSpanId;
        public Event[] events;
        public Attribute[] attributes;
    }

    public static class InstrumentationLibrary {
        public String name;
        public String version;
    }

    public static class InstrumentationLibrarySpan {
        public Span[] spans;
        public InstrumentationLibrary instrumentationLibrary;
    }

    public static class Resource {
        public Attribute[] attributes;
    }

    public static class ResourceSpan {
        public InstrumentationLibrarySpan[] instrumentationLibrarySpans;
        public Resource resource;
    }

    public ResourceSpan[] resourceSpans;
}
