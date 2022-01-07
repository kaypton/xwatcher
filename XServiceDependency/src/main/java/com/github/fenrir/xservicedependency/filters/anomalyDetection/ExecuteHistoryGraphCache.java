package com.github.fenrir.xservicedependency.filters.anomalyDetection;

import com.github.fenrir.xservicedependency.filters.anomalyDetection.ExecuteHistoryGraph;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ExecuteHistoryGraphCache {
    private Map<String, ExecuteHistoryGraph> cacheMap = new ConcurrentHashMap<>();


}
