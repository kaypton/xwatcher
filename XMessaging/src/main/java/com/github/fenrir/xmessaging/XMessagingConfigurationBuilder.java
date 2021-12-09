package com.github.fenrir.xmessaging;

public class XMessagingConfigurationBuilder {

    private String natsAddresses = null;

    public XMessagingConfigurationBuilder setNatsAddresses(String addresses){
        this.natsAddresses = addresses;
        return this;
    }

    public XMessagingConfiguration build(){
        if(this.natsAddresses == null){
            // TODO Throw Exception
            return null;
        }

        return new XMessagingConfiguration(this.natsAddresses);
    }

    static public XMessagingConfigurationBuilder builder(){
        return new XMessagingConfigurationBuilder();
    }
}
