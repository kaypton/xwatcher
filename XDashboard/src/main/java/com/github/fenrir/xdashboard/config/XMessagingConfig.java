package com.github.fenrir.xdashboard.config;

import com.github.fenrir.xmessaging.XMessaging;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Order(value = 0)
@Configuration
public class XMessagingConfig {
    public XMessagingConfig(@Value("${dashboard.natsAddress}") String natsAddress){
        XMessaging.init(natsAddress);
    }
}
