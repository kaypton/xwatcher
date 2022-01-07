package com.github.fenrir.prometheusclient.entities;

import com.github.fenrir.xcommon.utils.Tuple2;

import java.util.List;
import java.util.Set;

public interface PrometheusQueryResult {
    int valueNum();
    String getName();
    Set<String> getLabelNames();
    String getLabel(String name);
    List<Tuple2<Double, Double>> getValues();
    String toString();
}
