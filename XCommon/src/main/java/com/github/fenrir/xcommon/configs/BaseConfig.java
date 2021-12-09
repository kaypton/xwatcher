package com.github.fenrir.xcommon.configs;

import com.github.fenrir.xcommon.configs.annotations.Section;
import com.github.fenrir.xcommon.configs.exceptions.ClassDoNotMatchException;

import java.util.HashMap;
import java.util.Map;

public class BaseConfig {
    private Map<String, Map<String, Object>> sectionMap;

    public BaseConfig(){
        this.sectionMap = new HashMap<>();


    }

    @SuppressWarnings("unchecked")
    public <T> T getOptOrDefault(String sectionName,
                                 String optName,
                                 T def,
                                 Class<T> clazz)
    throws ClassDoNotMatchException{
        if(this.sectionMap.containsKey(sectionName)){
            if(this.sectionMap.get(sectionName).containsKey(optName)){
                if(this.sectionMap.get(sectionName).get(optName).getClass() == clazz){
                    return (T) this.sectionMap.get(sectionName).get(optName);
                }else{
                    throw new ClassDoNotMatchException();
                }
            }else{
                return def;
            }
        }else{
            return def;
        }
    }
}
