package com.github.fenrir.xservicedependency.processors.anomalyDetection.config;

import java.util.Map;

public class Configuration {
    private Map<String, Map<String, TimeLimit>> responseTimeLimits;


    public Map<String, Map<String, TimeLimit>> getResponseTimeLimits() {
        return responseTimeLimits;
    }

    public void setResponseTimeLimits(Map<String, Map<String, TimeLimit>> responseTimeLimits) {
        this.responseTimeLimits = responseTimeLimits;
    }
}
