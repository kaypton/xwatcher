package com.github.fenrir.xmessaging.test;

import com.github.fenrir.xmessaging.XMessaging;
import com.github.fenrir.xmessaging.rpc.XMessagingRpcClient;
import com.github.fenrir.xmessaging.rpc.annotation.RpcServerScan;
import com.github.fenrir.xmessaging.rpc.exceptions.ErrorStatusException;
import com.github.fenrir.xmessaging.rpc.exceptions.NotRpcReturnMessageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RpcServerScan(path = "com.github.fenrir.xmessaging.test.rpc")
public class Test {
    private static final Logger logger = LoggerFactory.getLogger("Test");
    static public void main(String[] args){
        XMessaging.initRpc(Test.class, "Test",
                "nats://222.201.144.196:4222");

        XMessagingRpcClient rpcClient = XMessaging.createRpcClient("Test");
        String retVal = null;
        try {
            retVal = rpcClient.call("TestRpcServer", "append", String.class, "hello", "world");
        } catch (ErrorStatusException | NotRpcReturnMessageException e) {
            e.printStackTrace();
        }
        logger.info(retVal);
    }
}
