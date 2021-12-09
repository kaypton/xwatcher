package com.github.fenrir.xsniffer.exporter;

import com.github.fenrir.xmessaging.XMessage;

public interface Exporter {
    void export(XMessage msg);
}
