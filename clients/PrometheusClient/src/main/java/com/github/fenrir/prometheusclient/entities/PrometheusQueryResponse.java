package com.github.fenrir.prometheusclient.entities;

import com.github.fenrir.prometheusclient.entities.impl.PrometheusQueryResponseImpl;

import java.util.List;

public interface PrometheusQueryResponse {
    int resultsNum();
    List<PrometheusQueryResult> getResults();

    static PrometheusQueryResponseBuilder newBuilder() {
        return PrometheusQueryResponseImpl.newBuilder();
    }
}
