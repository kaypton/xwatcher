package com.github.fenrir.prometheusclient.services;

import com.github.fenrir.prometheusclient.entities.PrometheusQueryResponse;
import org.springframework.context.ConfigurableApplicationContext;

public interface PrometheusDataService {
    void init(ConfigurableApplicationContext context);
    PrometheusQueryResponse query(String query, Long timestamp);
}
