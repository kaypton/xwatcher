package com.github.fenrir.prometheusdata;

@FunctionalInterface
public interface GetDataMethod {
    Double getData(String metricName, String... extension);
}
