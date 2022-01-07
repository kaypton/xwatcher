package com.github.fenrir.xmessaging.drivers.nats.driver;

public class XNatsSettings {
    public enum ConsumeMode {
        ASYNC,
        SYNC
    }

    public enum PublishMode {
        Publish
    }

    public enum MessageType {
        JSON
    }
}
