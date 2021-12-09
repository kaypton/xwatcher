package com.github.fenrir.xlocalmonitor.services.prometheus;

@FunctionalInterface
public interface GetDataMethod {
    Double getData(String metricName, String... extension);
}
