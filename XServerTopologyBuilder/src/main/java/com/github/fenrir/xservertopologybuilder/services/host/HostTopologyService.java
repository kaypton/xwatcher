package com.github.fenrir.xservertopologybuilder.services.host;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.github.fenrir.xcommon.utils.CommonUtils;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class HostTopologyService {

    public static class Host {
        /**
         * Physical Machine name
         */
        @JSONField(name="hostname")
        @Getter @Setter private String hostname = null;

        /**
         * cpu core name
         */
        @JSONField(name="cpu.core.num")
        @Getter @Setter private Integer cpuCoreNum = -1;

        /**
         * total memory size
         */
        @JSONField(name="mem.total.KiB")
        @Getter @Setter private Long memoryTotalKb = (long) -1;

        @JSONField(name = "cpu.total.usage")
        @Getter @Setter private Double cpuTotalUtil = -1.0;

        @JSONField(name = "cpu.system.usage")
        @Getter @Setter private Double cpuSystemUtil = -1.0;

        /**
         * cpu user usage
         */
        @JSONField(name="cpu.user.usage")
        @Getter @Setter private Double cpuUserUtil = -1.0;

        /**
         * GUEST CPU 利用率
         */
        @JSONField(name="cpu.guest.usage")
        @Getter @Setter private Double cpuGuestUtil = -1.0;

        /**
         * 可用内存空间大小
         */
        @JSONField(name="mem.free.KiB")
        @Getter @Setter private Double ramFree = -1.0;

        /**
         * 所有虚拟机的 CPU usage
         */
        @JSONField(name="vm.cpu.usage")
        @Getter @Setter private Double vmCpuUsage = -1.0;

        /**
         * 除了虚拟机的其他 app 的 CPU usage
         */
        @JSONField(name="idle.cpu.usage")
        @Getter @Setter private Double idleCpuUsage = -1.0;

        /**
         * 最后一次更新时间戳
         */
        @JSONField(name="timestamp")
        @Getter @Setter private long timestamp = 0;

        public void update(String metricName, Object value){
            switch (metricName) {
                case "cpu.user.usage":
                    if (value.getClass() == Double.class) {
                        this.setCpuUserUtil((Double) value);
                    }
                    this.setTimestamp(CommonUtils.getTimestamp());
                    break;
                case "cpu.guest.usage":
                    if (value.getClass() == Double.class) {
                        this.setCpuGuestUtil((Double) value);
                    }
                    this.setTimestamp(CommonUtils.getTimestamp());
                    break;
                case "mem.free.KiB":
                    if (value.getClass() == Double.class) {
                        this.setRamFree((Double) value);
                    }
                    this.setTimestamp(CommonUtils.getTimestamp());
                    break;
                case "vm.cpu.usage":
                    if(value.getClass() == Double.class){
                        this.setVmCpuUsage((Double) value);
                    }
                    this.setTimestamp(CommonUtils.getTimestamp());
                    break;
                case "idle.cpu.usage":
                    if(value.getClass() == Double.class){
                        this.setIdleCpuUsage((Double) value);
                    }
                    this.setTimestamp(CommonUtils.getTimestamp());
                case "cpu.core.num":
                    if(value.getClass() == Integer.class){
                        this.setCpuCoreNum((Integer) value);
                    }
                    this.setTimestamp(CommonUtils.getTimestamp());
                case "mem.total.KiB":
                    if(value.getClass() == Long.class){
                        this.setMemoryTotalKb((long) value);
                    }
                    this.setTimestamp(CommonUtils.getTimestamp());
                case "cpu.system.usage":
                    if(value.getClass() == Double.class){
                        this.setCpuSystemUtil((double) value);
                    }
                    this.setTimestamp(CommonUtils.getTimestamp());
                case "cpu.total.usage":
                    if(value.getClass() == Double.class){
                        this.setCpuTotalUtil((double) value);
                    }
                    this.setTimestamp(CommonUtils.getTimestamp());
            }
        }
    }

    // hostname -> Host instance
    @Getter @Setter private Map<String, Host> hostMap = new ConcurrentHashMap<>();

    public void report(String hostname,
                       String metricName,
                       Object value){
        Host host = getHostMap().get(hostname);

        if(host == null){
            Host _host = new Host();
            _host.setHostname(hostname);
            _host.update(metricName, value);
            getHostMap().put(hostname, _host);
        }else{
            host.update(metricName, value);
        }
    }

    public String getAll(){
        JSONObject ret = new JSONObject();
        for(String hostname : getHostMap().keySet()){
            ret.put(hostname, getHostMap().get(hostname));
        }
        return ret.toJSONString();
    }

    public List<Host> getHostCpuGuestUtilHigherThan(Double threshold,
                                                    Boolean equal){
        List<Host> hostList = new ArrayList<>();
        for(String hostname : this.getHostMap().keySet()){
            if(this.getHostMap().get(hostname).getCpuGuestUtil() > threshold){
                hostList.add(this.getHostMap().get(hostname));
            }
            if(equal) if(this.getHostMap().get(hostname).getCpuGuestUtil().equals(threshold)){
                hostList.add(this.getHostMap().get(hostname));
            }
        }
        return hostList;
    }

    public List<Host> getHostCpuGuestUtilLessThan(Double threshold,
                                                  Boolean equal){
        List<Host> hostList = new ArrayList<>();
        for(String hostname : this.getHostMap().keySet()){
            if(this.getHostMap().get(hostname).getCpuGuestUtil() < threshold){
                hostList.add(this.getHostMap().get(hostname));
            }
            if(equal) if(this.getHostMap().get(hostname).getCpuGuestUtil().equals(threshold)){
                hostList.add(this.getHostMap().get(hostname));
            }
        }
        return hostList;
    }
}
