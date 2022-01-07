package com.github.fenrir.xservicedependency.filters;

import com.github.fenrir.xservicedependency.entities.serviceDependency.Span;

public abstract class Filter {

    private Filter nextFilter = null;

    protected abstract Span internalDoFilter(Span span);

    public void setNextFilter(Filter filter) {
        this.nextFilter = filter;
    }

    public void doFilter(Span span){
        if(this.nextFilter != null)
            this.nextFilter.doFilter(this.internalDoFilter(span));
    }
}
