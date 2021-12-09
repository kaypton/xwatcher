package com.github.fenrir.xmessaging.rpc;

import com.alibaba.fastjson.JSON;
import com.github.fenrir.xmessaging.*;
import com.github.fenrir.xmessaging.rpc.annotation.RpcServer;
import com.github.fenrir.xmessaging.rpc.exceptions.NoSuchFunctionException;
import com.github.fenrir.xmessaging.rpc.server.XMessagingRpcServerImpl;
import com.github.fenrir.xmessaging.rpc.types.RpcRequestMessage;
import com.github.fenrir.xmessaging.rpc.types.RpcReturnMessage;
import com.github.fenrir.xmessaging.rpc.types.RpcReturnStatus;
import com.github.fenrir.xmessaging.rpc.types.XMessagingRpcConstants;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class XMessagingRpcServerPool implements MessageProcessCallBack {
    static private final Logger logger = LoggerFactory.getLogger("XMessagingRpcServerPool");

    private final Map<String, XMessagingRpcServer> serverMap;
    private final XMessagingPublisher publisher;

    private final ThreadPoolExecutor executor = new ThreadPoolExecutor(
            10, 20, 10, TimeUnit.SECONDS, new LinkedBlockingDeque<>()
    );

    private XMessagingRpcServerPool(Map<String, XMessagingRpcServer> serverMap,
                                    String serverName,
                                    XMessagingPublisher publisher){
        this.serverMap = serverMap;
        this.publisher = publisher;
    }

    @Override
    public void processMessage(XMessage msg) {
        if(!msg.getNatsRawMessage().getHeaders().get(XMessagingRpcConstants.XMESSAGING_RPC_ROLE).get(0)
            .equals(XMessagingRpcConstants.XMESSAGING_RPC_ROLE_REQUEST)){
            return;
        }
        RpcRequestMessage requestMessage = JSON.parseObject(
                msg.getStringData(), RpcRequestMessage.class
        );
        this.executor.execute(new Executor(
                requestMessage,
                msg.getNatsRawMessage().getReplyTo(),
                this.serverMap,
                this.publisher
        ));
    }

    static public void init(String[] packagePaths, String serverName, XMessaging x){
        Map<String, XMessagingRpcServer> serverMap = new ConcurrentHashMap<>();

        for(String path : packagePaths){
            Reflections reflections = new Reflections(path);
            Set<Class<?>>  classes = reflections.getTypesAnnotatedWith(RpcServer.class);

            for(Class<?> clazz : classes){
                logger.info("Scan " + clazz.getTypeName() + " for RPC Server");
                try {
                    Object server = clazz.getDeclaredConstructor().newInstance();
                    XMessagingRpcServer XMessagingRpcServer = XMessagingRpcServerImpl.create(server);
                    serverMap.put(XMessagingRpcServer.getServerName(), XMessagingRpcServer);
                } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }

        XMessagingRpcServerPool pool = new XMessagingRpcServerPool(serverMap, serverName, XMessaging.createPublisher(null));
        String rpcServerTopicName = XMessagingRpcConstants.XMESSAGING_RPC_SERVER_TOPIC_PREFIX + serverName;
        if(x == null)
            XMessaging.createListener(rpcServerTopicName, pool);
        else x.getListener(rpcServerTopicName, pool);
    }

    private static class Executor implements Runnable {

        private final RpcRequestMessage rpcRequestMessage;
        private final Map<String, XMessagingRpcServer> serverMap;
        private final XMessagingPublisher publisher;
        private final String replyTo;

        public Executor(RpcRequestMessage rpcRequestMessage,
                        String replyTo,
                        Map<String, XMessagingRpcServer> serverMap,
                        XMessagingPublisher publisher){

            logger.info("rpc call:");
            logger.info("    dstFunction: " + rpcRequestMessage.dstFunction);
            logger.info("    dstServer  : " + rpcRequestMessage.dstServer);
            logger.info("    parameters : " + Arrays.toString(rpcRequestMessage.parameters.toArray(new Object[0])));
            logger.info("    return type: " + rpcRequestMessage.returnTypeName);

            this.rpcRequestMessage = rpcRequestMessage;
            this.serverMap = serverMap;
            this.publisher = publisher;
            this.replyTo = replyTo;
        }

        @Override
        public void run() {

            // 构建目标函数 ID
            StringBuilder functionIDBuilder = new StringBuilder();
            functionIDBuilder.append(this.rpcRequestMessage.dstFunction)
                    .append(XMessagingRpcConstants.XMESSAGING_RPC_FUNCTION_ID_SEPARATOR);
            for(String parameterTypeName: rpcRequestMessage.parametersTypeName) {
                functionIDBuilder.append(parameterTypeName)
                        .append(XMessagingRpcConstants.XMESSAGING_RPC_FUNCTION_ID_SEPARATOR);
            }
            functionIDBuilder.append(rpcRequestMessage.returnTypeName);

            if(this.serverMap.containsKey(rpcRequestMessage.dstServer)){  // 如果目标 server 存在
                XMessagingRpcServer server = this.serverMap.get(rpcRequestMessage.dstServer);
                try {
                    RpcReturnMessage<Object> returnMessage = new RpcReturnMessage<>();
                    Object retVal = server.call(functionIDBuilder.toString(),
                            rpcRequestMessage.parameters.toArray(new Object[0]));
                    returnMessage.status = RpcReturnStatus.OK;
                    returnMessage.retVal = retVal;

                    logger.info("return message: {}", JSON.toJSONString(returnMessage));

                    XMessage msg = XMessageBuilder.builder(this.replyTo)
                            .setStringPayload(JSON.toJSONString(returnMessage))
                            .addHeader(XMessagingRpcConstants.XMESSAGING_RPC_ROLE
                                    , XMessagingRpcConstants.XMESSAGING_RPC_ROLE_RETURN)
                            .buildNatsMessage();

                    this.publisher.send(msg);

                } catch (NoSuchFunctionException e) {
                    e.printStackTrace();
                    RpcReturnMessage<Object> returnMessage = new RpcReturnMessage<>();
                    returnMessage.status = RpcReturnStatus.NoSuchFunction;

                    XMessage msg = XMessageBuilder.builder(this.replyTo)
                            .setStringPayload(JSON.toJSONString(returnMessage))
                            .addHeader(XMessagingRpcConstants.XMESSAGING_RPC_ROLE
                                    , XMessagingRpcConstants.XMESSAGING_RPC_ROLE_RETURN)
                            .buildNatsMessage();

                    this.publisher.send(msg);
                }
            }else{ // 如果目标 server 不存在
                RpcReturnMessage<Object> returnMessage = new RpcReturnMessage<>();
                returnMessage.status = RpcReturnStatus.NoSuchServer;

                XMessage msg = XMessageBuilder.builder(this.replyTo)
                        .setStringPayload(JSON.toJSONString(returnMessage))
                        .addHeader(XMessagingRpcConstants.XMESSAGING_RPC_ROLE
                                , XMessagingRpcConstants.XMESSAGING_RPC_ROLE_RETURN)
                        .buildNatsMessage();

                this.publisher.send(msg);
            }
        }
    }
}
