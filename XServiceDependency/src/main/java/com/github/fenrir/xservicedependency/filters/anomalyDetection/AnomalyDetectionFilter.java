package com.github.fenrir.xservicedependency.filters.anomalyDetection;

import com.github.fenrir.xservicedependency.entities.serviceDependency.Span;
import com.github.fenrir.xservicedependency.filters.Filter;

public class AnomalyDetectionFilter extends Filter {
    @Override
    protected Span internalDoFilter(Span span) {
        return span;
    }
}
