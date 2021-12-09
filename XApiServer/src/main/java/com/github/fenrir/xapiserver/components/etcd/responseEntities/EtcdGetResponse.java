package com.github.fenrir.xapiserver.components.etcd.responseEntities;

import com.github.fenrir.xcommon.utils.Tuple2;

import java.util.ArrayList;
import java.util.List;

public class EtcdGetResponse extends BaseResponse {
    private final List<Tuple2<String, String>> kvList = new ArrayList<>();

    public EtcdGetResponse(){

    }

    public void addKv(String key, String value){
        this.kvList.add(new Tuple2<>(key, value));
    }

    public int getSize(){
        return kvList.size();
    }

    public String getKey(int index){
        if(index >= kvList.size()){
            return null;
        }
        return kvList.get(index).first;
    }

    public String getValue(int index){
        if(index >= kvList.size()){
            return null;
        }
        return kvList.get(index).second;
    }
}
