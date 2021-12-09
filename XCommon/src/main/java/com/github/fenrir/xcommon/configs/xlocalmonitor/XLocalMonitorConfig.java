package com.github.fenrir.xcommon.configs.xlocalmonitor;

import com.github.fenrir.xcommon.configs.annotations.Option;
import com.github.fenrir.xcommon.configs.annotations.Section;

public class XLocalMonitorConfig {

    @Section(name = "nats", usage = "")
    public static class Nats{
        @Option(name = "nats_address", need = true, usage = "")
        public String natsAddress;
    }
}
