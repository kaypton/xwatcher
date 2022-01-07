package com.github.fenrir.xservicedependency.entities.influxDB;

import com.influxdb.annotations.Column;
import com.influxdb.annotations.Measurement;

import java.time.Instant;

@Measurement(name = "interface_st_nano")
public class InterfaceSTNano {
    @Column(tag = true)
    public String serviceName;

    @Column(tag = true)
    public String interfaceName;

    @Column
    public double value;

    @Column(timestamp = true)
    public Instant time;
}
