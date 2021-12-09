package com.github.fenrir.xmessaging.rpc.function;

import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

public class XMessagingRpcFunction {
    private static final Logger logger = LoggerFactory.getLogger("XMessagingRpcFunction");

    @Getter @Setter private Object srcObject = null;
    /**
     * method instance
     */
    @Getter @Setter private Method method = null;

    private XMessagingRpcFunction(Object srcObject, Method method){
        this.setSrcObject(srcObject);
        this.setMethod(method);
    }

    public Object call(Object... args){
        try {
            logger.info("call args : " + Arrays.toString(args));
            return this.getMethod().invoke(this.getSrcObject(), args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return null;
        }
    }

    static public XMessagingRpcFunction create(Object srcObject, Method method){
        assert method != null;
        assert srcObject != null;

        return new XMessagingRpcFunction(srcObject, method);
    }
}
