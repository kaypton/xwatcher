package com.github.fenrir.xservicedependency.entities.influxDB;

import com.influxdb.annotations.Column;
import com.influxdb.annotations.Measurement;

import java.time.Instant;

@Measurement(name = "interface_rt_nano")
public class InterfaceRTNano {
    @Column(tag = true)
    public String serviceName;

    @Column(tag = true)
    public String interfaceName;

    @Column(tag = true)
    public String srcServiceName;

    @Column(tag = true)
    public String srcInterfaceName;

    @Column
    public double value;

    @Column(timestamp = true)
    public Instant time;
}
