package com.github.fenrir.xapiserver.components.etcd.responseEntities;

import com.github.fenrir.xcommon.utils.Tuple2;

import java.util.ArrayList;
import java.util.List;

public class EtcdDeleteResponse extends BaseResponse{
    private List<Tuple2<String, String>> preKv = null;

    public EtcdDeleteResponse(){

    }

    public int preKvSize(){
        return preKv.size();
    }

    public void addPreKv(String key, String value){
        if(this.preKv == null)
            this.preKv = new ArrayList<>();
        this.preKv.add(new Tuple2<>(key, value));
    }

    public String getPreKey(int index){
        return preKv.get(index).first;
    }

    public String getPreValue(int index){
        return preKv.get(index).second;
    }
}
