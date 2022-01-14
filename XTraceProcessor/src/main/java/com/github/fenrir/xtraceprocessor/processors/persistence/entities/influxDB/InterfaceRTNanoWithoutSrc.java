package com.github.fenrir.xtraceprocessor.processors.persistence.entities.influxDB;

import com.influxdb.annotations.Column;
import com.influxdb.annotations.Measurement;

import java.time.Instant;

@Measurement(name = "interface_rt_nano_without_src")
public class InterfaceRTNanoWithoutSrc {
    @Column(tag = true)
    public String serviceName;

    @Column(tag = true)
    public String interfaceName;

    @Column(tag = true)
    public String interfaceURI;

    @Column(tag = true)
    public String version;

    @Column
    public double value;

    @Column(timestamp = true)
    public Instant time;
}
