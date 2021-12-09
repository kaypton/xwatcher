package com.github.fenrir.xmessaging.rpc.server;

import com.github.fenrir.xmessaging.rpc.XMessagingRpcServer;
import com.github.fenrir.xmessaging.rpc.annotation.RpcServer;
import com.github.fenrir.xmessaging.rpc.exceptions.NoSuchFunctionException;
import com.github.fenrir.xmessaging.rpc.function.XMessagingRpcFunction;
import com.github.fenrir.xmessaging.rpc.types.XMessagingRpcConstants;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class XMessagingRpcServerImpl implements XMessagingRpcServer {
    private static final Logger LOGGER = LoggerFactory.getLogger("XMessagingRpcServerImpl");
    @Getter @Setter private String serverName = null;
    @Getter @Setter private Map<String, XMessagingRpcFunction> functionMap = null;

    private XMessagingRpcServerImpl(Map<String, XMessagingRpcFunction> functionMap, Object srcObject){
        assert functionMap != null;
        assert srcObject != null;

        this.setFunctionMap(functionMap);

        RpcServer rpcServerAnnotation = srcObject.getClass().getDeclaredAnnotation(RpcServer.class);
        if(!rpcServerAnnotation.name().equals("")){
            this.setServerName(rpcServerAnnotation.name());
        }else
            this.setServerName(srcObject.getClass().getSimpleName());
        for(String methodId : this.functionMap.keySet()){
            LOGGER.info("function id: {} in server {}", methodId, this.serverName);
        }
    }

    @Override
    public Object call(String functionID, Object... args) throws NoSuchFunctionException {
        if(this.getFunctionMap().containsKey(functionID)){
            return this.getFunctionMap().get(functionID).call(args);
        }else{
            throw new NoSuchFunctionException();
        }
    }

    static public XMessagingRpcServer create(Object server){
        Map<String, XMessagingRpcFunction> functionMap = new ConcurrentHashMap<>();

        Method[] methods = server.getClass().getDeclaredMethods();
        for(Method method : methods){
            String methodId = getMethodID(method);
            functionMap.put(methodId, XMessagingRpcFunction.create(server, method));
        }

        return new XMessagingRpcServerImpl(functionMap, server);
    }

    // functionName_param1_param2_returnType
    static private String getMethodID(Method method){
        StringBuilder functionNameBuilder = new StringBuilder();
        functionNameBuilder.append(method.getName())
                .append(XMessagingRpcConstants.XMESSAGING_RPC_FUNCTION_ID_SEPARATOR);
        Class<?>[] parameterTypes = method.getParameterTypes();
        for(Class<?> parameterType : parameterTypes){
            functionNameBuilder.append(parameterType.getTypeName())
                    .append(XMessagingRpcConstants.XMESSAGING_RPC_FUNCTION_ID_SEPARATOR);
        }
        functionNameBuilder.append(method.getReturnType().getTypeName());
        return functionNameBuilder.toString();
    }
}
