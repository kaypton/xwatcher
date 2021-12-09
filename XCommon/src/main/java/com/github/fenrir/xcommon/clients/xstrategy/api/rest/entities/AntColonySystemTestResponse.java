package com.github.fenrir.xcommon.clients.xstrategy.api.rest.entities;

import com.github.fenrir.xcommon.clients.BaseResponse;

import java.util.Map;

public class AntColonySystemTestResponse<T, K> extends BaseResponse {
    public Map<String, T> hosts;
    public Map<String, K> parasite;
}
