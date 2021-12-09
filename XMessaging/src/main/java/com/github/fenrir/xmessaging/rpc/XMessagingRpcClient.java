package com.github.fenrir.xmessaging.rpc;

import com.github.fenrir.xmessaging.XMessage;
import com.github.fenrir.xmessaging.rpc.exceptions.ErrorStatusException;
import com.github.fenrir.xmessaging.rpc.exceptions.NotRpcReturnMessageException;

public interface XMessagingRpcClient {
    <T> T call(String server, String function, Class<T> returnType, Object... args) throws ErrorStatusException, NotRpcReturnMessageException;
    XMessage cast(String server, String function, Class<?> returnType, Object... args);
}
