package com.github.fenrir.xservicedependency.processors;

import com.github.fenrir.xservicedependency.entities.serviceDependency.Span;

public abstract class Processor {

    private Processor nextFilter = null;

    protected abstract Span internalDoProcess(Span span);

    public void setNextFilter(Processor filter) {
        this.nextFilter = filter;
    }

    public void doProcess(Span span) {
        span = this.internalDoProcess(span);
        if(this.nextFilter != null)
            this.nextFilter.doProcess(span);
    }
}
