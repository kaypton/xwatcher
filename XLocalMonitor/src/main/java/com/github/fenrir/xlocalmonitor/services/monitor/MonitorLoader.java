package com.github.fenrir.xlocalmonitor.services.monitor;

import org.springframework.stereotype.Service;

import java.net.URLClassLoader;

@Service
public class MonitorLoader {
    static private void test(){
        Thread.currentThread().getContextClassLoader();
    }
}
