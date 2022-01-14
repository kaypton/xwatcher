package com.github.fenrir.xdataflowtrace.functions;

import com.github.fenrir.xdataflowtrace.configs.URISelectorConfig;
import com.github.fenrir.xdataflowtrace.entity.SpanWithCount;
import com.github.fenrir.xdataflowtrace.entity.trace.Span;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.util.Collector;

public class SpanFlatMapFunction implements FlatMapFunction<Span, SpanWithCount> {
    private final URISelectorConfig selectorConfig;
    public SpanFlatMapFunction(URISelectorConfig selectorConfig){
        this.selectorConfig = selectorConfig;
    }
    @Override
    public void flatMap(Span span, Collector<SpanWithCount> out) {
        String interfaceURI = selectorConfig.match(span.getServiceName(), span.getInterfaceURI());
        span.setInterfaceName(interfaceURI);
        span.setInterfaceURI(interfaceURI);
        out.collect(new SpanWithCount(span));
    }
}
