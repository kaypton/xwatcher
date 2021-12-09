package com.github.fenrir.xmessaging.test.rpc;

import com.github.fenrir.xmessaging.rpc.annotation.RpcServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RpcServer
public class TestRpcServer {
    private static final Logger logger = LoggerFactory.getLogger("TestRpcServer");
    public String append(String a, String b){
        logger.info("append function has been called");
        return a + b;
    }
}
