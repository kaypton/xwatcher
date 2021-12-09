package com.github.fenrir.xmessaging.rpc.types;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.List;

public class RpcRequestMessage {
    @JSONField(name = "parameters")
    public List<Object> parameters;

    @JSONField(name = "parametersTypeName")
    public List<String> parametersTypeName;

    @JSONField(name = "dstServer")
    public String dstServer;

    @JSONField(name = "dstFunction")
    public String dstFunction;

    @JSONField(name = "returnTypeName")
    public String returnTypeName;
}
