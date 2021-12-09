package com.github.fenrir.xmessaging.rpc;

import com.github.fenrir.xmessaging.rpc.exceptions.NoSuchFunctionException;

public interface XMessagingRpcServer {
    String getServerName();
    Object call(String functionName, Object... args) throws NoSuchFunctionException;
}
