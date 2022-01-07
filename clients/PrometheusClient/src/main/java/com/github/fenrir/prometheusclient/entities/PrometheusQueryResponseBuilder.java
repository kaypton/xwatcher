package com.github.fenrir.prometheusclient.entities;

public interface PrometheusQueryResponseBuilder {
    PrometheusQueryResponseBuilder withResult(PrometheusQueryResult result);
    PrometheusQueryResponse build();
}
