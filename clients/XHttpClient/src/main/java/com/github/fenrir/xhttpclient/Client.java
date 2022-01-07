package com.github.fenrir.xhttpclient;

import java.util.Map;

public interface Client {
    <T> XHttpResponse<T> get(String url, Map<String, String> headers, Map<String, String> params, String body, Class<T> clazz);
}
