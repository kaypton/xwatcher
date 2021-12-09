package com.github.fenrir.xlocalmonitor.inspectors.local.memory.linux;

import com.github.fenrir.xlocalmonitor.annotations.Inspector;
import oshi.SystemInfo;
import oshi.hardware.GlobalMemory;

@Inspector(name = "oshi.global.memory")
public class OshiGlobalMemory {
    private final GlobalMemory globalMemory;

    public OshiGlobalMemory(){
        this.globalMemory = new SystemInfo().getHardware().getMemory();
    }

    public long getAvailable(){
        // ThreadSafe
        return this.globalMemory.getAvailable();
    }

    public long getTotal(){
        // ThreadSafe
        return this.globalMemory.getTotal();
    }

    public int getPhysicalMemoryNum(){
        // ThreadSafe
        return this.globalMemory.getPhysicalMemory().size();
    }
}
