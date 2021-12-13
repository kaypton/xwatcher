package com.github.fenrir.xservicedependency.entities.trace;

public class OpenTelemetryTraceData {

    public static class Event {
        public String timeUnixNano;
        public String name;
    }

    public static class Span {
        public String traceId;
        public String spanId;
        public String endTimeUnixNano;
        public String kind;
        public String name;
        public String startTimeUnixNano;
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
