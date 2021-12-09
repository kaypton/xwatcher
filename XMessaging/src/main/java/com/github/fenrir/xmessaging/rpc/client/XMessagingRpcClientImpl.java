package com.github.fenrir.xmessaging.rpc.client;

import com.alibaba.fastjson.JSON;
import com.github.fenrir.xmessaging.XMessage;
import com.github.fenrir.xmessaging.XMessageBuilder;
import com.github.fenrir.xmessaging.XMessaging;
import com.github.fenrir.xmessaging.XMessagingPublisher;
import com.github.fenrir.xmessaging.exceptions.NoSuchTypeHeaderException;
import com.github.fenrir.xmessaging.rpc.XMessagingRpcClient;
import com.github.fenrir.xmessaging.rpc.exceptions.ErrorStatusException;
import com.github.fenrir.xmessaging.rpc.exceptions.NotRpcReturnMessageException;
import com.github.fenrir.xmessaging.rpc.types.RpcRequestMessage;
import com.github.fenrir.xmessaging.rpc.types.RpcReturnMessage;
import com.github.fenrir.xmessaging.rpc.types.RpcReturnStatus;
import com.github.fenrir.xmessaging.rpc.types.XMessagingRpcConstants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class XMessagingRpcClientImpl implements XMessagingRpcClient {

    private final XMessagingPublisher publisher;
    private final String serverTopic;

    private XMessagingRpcClientImpl(String server){
        this.publisher = XMessaging.createPublisher(XMessagingRpcConstants.XMESSAGING_RPC_SERVER_TOPIC_PREFIX
                + server);
        this.serverTopic = XMessagingRpcConstants.XMESSAGING_RPC_SERVER_TOPIC_PREFIX + server;
    }

    private XMessagingRpcClientImpl(String server, XMessaging x){
        this.publisher = x.getPublisher(XMessagingRpcConstants.XMESSAGING_RPC_SERVER_TOPIC_PREFIX +
                server);
        this.serverTopic = XMessagingRpcConstants.XMESSAGING_RPC_SERVER_TOPIC_PREFIX + server;
    }

    @Override
    public <T> T call(String server, String function, Class<T> returnType, Object... args)
            throws ErrorStatusException, NotRpcReturnMessageException {
        RpcRequestMessage requestMessage = new RpcRequestMessage();
        requestMessage.dstServer = server;
        this.assembleParameters(requestMessage, args);
        requestMessage.dstFunction = function;
        requestMessage.returnTypeName = returnType.getTypeName();

        XMessage msg = XMessageBuilder.builder(this.serverTopic)
                .addHeader(XMessagingRpcConstants.XMESSAGING_RPC_ROLE,
                        XMessagingRpcConstants.XMESSAGING_RPC_ROLE_REQUEST)
                .setStringPayload(JSON.toJSONString(requestMessage))
                .buildNatsMessage();

        XMessage retXMsg = this.publisher.request(msg);
        String rpcMessageRole;
        try {
            rpcMessageRole = retXMsg.getHeader(XMessagingRpcConstants.XMESSAGING_RPC_ROLE, String.class);
        } catch (NoSuchTypeHeaderException e) {
            e.printStackTrace();
            return null;
        }

        if(rpcMessageRole.equals(XMessagingRpcConstants.XMESSAGING_RPC_ROLE_RETURN)){
            RpcReturnMessage<T> retMsg = RpcReturnMessage.create(retXMsg.getStringData(), returnType);
            if(!retMsg.status.equals(RpcReturnStatus.OK)){
                throw new ErrorStatusException("rpc return status : " + retMsg.status);
            }else{
                return retMsg.retVal;
            }
        }else{
            throw new NotRpcReturnMessageException();
        }
    }

    @Override
    public XMessage cast(String server, String function, Class<?> returnType, Object... args) {
        RpcRequestMessage requestMessage = new RpcRequestMessage();
        requestMessage.dstServer = server;
        this.assembleParameters(requestMessage, args);
        requestMessage.dstFunction = function;
        requestMessage.returnTypeName = returnType.getTypeName();

        XMessage msg = XMessageBuilder.builder(this.serverTopic)
                .addHeader(XMessagingRpcConstants.XMESSAGING_RPC_ROLE,
                        XMessagingRpcConstants.XMESSAGING_RPC_ROLE_REQUEST)
                .setStringPayload(JSON.toJSONString(requestMessage))
                .buildNatsMessage();

        return this.publisher.asyncRequest(msg);
    }

    private void assembleParameters(RpcRequestMessage requestMessage, Object... args){
        List<String> parametersTypeName = new ArrayList<>();
        for(Object arg : args){
            parametersTypeName.add(arg.getClass().getTypeName());
        }
        requestMessage.parametersTypeName = parametersTypeName;
        requestMessage.parameters = Arrays.asList(args);
    }

    static public XMessagingRpcClientImpl create(String server){
        return new XMessagingRpcClientImpl(server);
    }

    static public XMessagingRpcClientImpl create(String server, XMessaging x){
        return new XMessagingRpcClientImpl(server, x);
    }
}
