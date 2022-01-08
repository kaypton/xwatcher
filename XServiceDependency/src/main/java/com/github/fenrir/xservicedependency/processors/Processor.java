package com.github.fenrir.xservicedependency.processors;

import com.github.fenrir.xservicedependency.entities.serviceDependency.Span;

public abstract class Processor {

    private Processor nextFilter = null;

    protected abstract Span internalDoFilter(Span span);

    public void setNextFilter(Processor filter) {
        this.nextFilter = filter;
    }

    public void doFilter(Span span){
        if(this.nextFilter != null)
            this.nextFilter.doFilter(this.internalDoFilter(span));
    }
}
