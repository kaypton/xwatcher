package com.github.fenrir.xregistry.configs;

import com.github.fenrir.xmessaging.XMessaging;
import com.github.fenrir.xregistry.XRegistryApplication;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Order(value = 0)
@Configuration
public class XMessagingConfig {
    public XMessagingConfig(@Value("${XRegistry.rpc.natsAddress}") String natsAddress){
        XMessaging.initRpc(XRegistryApplication.class, "XRegistry", natsAddress);
    }
}
