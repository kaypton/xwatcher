package com.github.fenrir.xmessaging;

import com.github.fenrir.xmessaging.drivers.nats.driver.XNatsDriverFactory;
import com.github.fenrir.xmessaging.rpc.XMessagingRpcClient;
import com.github.fenrir.xmessaging.rpc.XMessagingRpcServerPool;
import com.github.fenrir.xmessaging.rpc.annotation.RpcServerScan;
import com.github.fenrir.xmessaging.rpc.client.XMessagingRpcClientImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

public class XMessaging implements Serializable {
    private static final Logger logger = LoggerFactory.getLogger(XMessaging.class);
    private static boolean init = false;

    static public void init(String natsServerAddresses){
        logger.info("XMessaging for XWatcher");
        XNatsDriverFactory.init(natsServerAddresses);
        init = true;
    }

    static public void initRpc(Class<?> anyClass, String serverName, String natsServerAddress){
        if(!init){
            if(natsServerAddress == null) {
                logger.error("first init XMessaging");
                return;
            }else XMessaging.init(natsServerAddress);
        }
        RpcServerScan rpcServerScan = anyClass.getDeclaredAnnotation(RpcServerScan.class);
        XMessagingRpcServerPool.init(rpcServerScan.path(), serverName, null);
    }

    static public XMessagingListener createListener(String topic,
                               MessageProcessCallBack messageProcessCallBack){
        return XNatsDriverFactory.createListener(topic, messageProcessCallBack);
    }

    static public XMessagingListener createListener(String topic){
        return XNatsDriverFactory.createListener(topic);
    }

    static public XMessagingPublisher createPublisher(String topic){
        return XNatsDriverFactory.createPublisher(topic);
    }

    static public XMessagingRpcClient createRpcClient(String server){
        return XMessagingRpcClientImpl.create(server);
    }

    /* ---------------------------------------------------------- */
    private final XNatsDriverFactory natsDriverFactory;

    public XMessaging(XMessagingConfiguration configuration){
        natsDriverFactory = XNatsDriverFactory.getInstance(configuration.getNatsAddresses());
    }

    public void initRpcServer(Class<?> anyClass, String serverName){
        RpcServerScan rpcServerScan = anyClass.getDeclaredAnnotation(RpcServerScan.class);
        XMessagingRpcServerPool.init(rpcServerScan.path(),
                serverName, this);
    }

    public XMessagingListener getListener(String topic){
        return this.natsDriverFactory.getListener(topic);
    }

    public XMessagingListener getListener(String topic,
                                          MessageProcessCallBack messageProcessCallBack){
        return this.natsDriverFactory.getListener(topic, messageProcessCallBack);
    }

    public XMessagingPublisher getPublisher(String topic){
        return this.natsDriverFactory.getPublisher(topic);
    }

    public XMessagingRpcClient getRpcClient(String server){
        return XMessagingRpcClientImpl.create(server, this);
    }

    static public XMessaging create(XMessagingConfiguration configuration){
        return new XMessaging(configuration);
    }
}
