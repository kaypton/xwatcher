package com.github.fenrir.xlocalmonitor.components;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class ApplicationContextUtil implements ApplicationContextAware {
    static private ApplicationContext applicationContext = null;

    @Override
    public void setApplicationContext(ApplicationContext _applicationContext) throws BeansException {
        applicationContext = _applicationContext;
    }

    static private ApplicationContext getApplicationContext(){
        return applicationContext;
    }

    static public Object getBean(String name){
        if(getApplicationContext() != null)
            return getApplicationContext().getBean(name);
        else return null;
    }

    static public <T> T getBean(Class<T> clazz){
        if(getApplicationContext() != null)
            return getApplicationContext().getBean(clazz);
        else return null;
    }

    static public <T> T getBean(String name, Class<T> clazz){
        if(getApplicationContext() != null)
            return getApplicationContext().getBean(name, clazz);
        else return null;
    }
}
