package com.github.fenrir.xmessaging.rpc.types;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;

public class RpcReturnMessage<T> {
    @JSONField(name = "retVal")
    public T retVal;

    @JSONField(name = "status")
    public Integer status;

    static public<RET> RpcReturnMessage<RET> create(String jsonString, Class<RET> returnType){
        RpcReturnMessage<RET> returnMessage = new RpcReturnMessage<>();
        JSONObject obj = JSON.parseObject(jsonString);

        returnMessage.status = obj.getIntValue("status");
        returnMessage.retVal = obj.getObject("retVal", returnType);
        return returnMessage;
    }
}
