package com.github.fenrir.xapiserver.configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfig {

    static public String etcdPrefix;

    public ApplicationConfig(@Value("${XApiServer.etcdPrefix}") String _etcdPrefix){
        etcdPrefix = _etcdPrefix;
        if(!etcdPrefix.endsWith("/"))
            etcdPrefix = etcdPrefix + "/";
        if(!etcdPrefix.startsWith("/"))
            etcdPrefix = "/" + etcdPrefix;
    }
}
