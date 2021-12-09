package com.github.fenrir.xsniffer.configs;

import lombok.Getter;
import lombok.Setter;
import org.kohsuke.args4j.Option;

public class ArgsConfig {
    @Option(name="--xregistry-address", usage="XRegistryService host address")
    @Getter @Setter private String xRegistryAddress = null;

    @Option(name="--nats-servers", usage="nats://127.0.0.1:4222,nats://x.x.x.x:4222")
    @Getter @Setter private String natsServerAddresses = null;

    @Option(name="--listen-to-stream", usage="listen to stream")
    @Getter @Setter private String listenToStream = null;

    @Option(name="--listen-to-event", usage="listen to event")
    @Getter @Setter private String listenToEvent = null;

    @Option(name="--output-format", usage="json or csv")
    @Getter @Setter private String outputFormat = null;

    @Option(name="--output-file", usage="/root/metric.csv")
    @Getter @Setter private String outputFile = null;
}
