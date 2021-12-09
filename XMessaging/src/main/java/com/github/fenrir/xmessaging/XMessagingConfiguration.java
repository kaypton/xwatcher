package com.github.fenrir.xmessaging;

import lombok.Getter;

public class XMessagingConfiguration {

    @Getter
    private final String natsAddresses;

    public XMessagingConfiguration(String natsAddresses){
        this.natsAddresses = natsAddresses;
    }
}
