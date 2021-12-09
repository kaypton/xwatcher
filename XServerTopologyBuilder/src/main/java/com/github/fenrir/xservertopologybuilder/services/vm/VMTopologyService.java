package com.github.fenrir.xservertopologybuilder.services.vm;

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
public class VMTopologyService {
    public static class VM {
        /**
         * 虚拟机所属物理机的主机名
         */
        @JSONField(name="hostname")
        @Getter @Setter private String hostname = null;

        /**
         * vm uuid
         */
        @JSONField(name="vm_uuid")
        @Getter @Setter private String vmUUID = null;

        /**
         * 虚拟机 display name<br/>
         * 在 OpenStack 中经常被起名为 instance-00000001 等等
         */
        @JSONField(name="vm_name")
        @Getter @Setter private String vmName = null;

        /**
         * 虚拟机申请的 cpu 核数
         */
        @JSONField(name="vcpus")
        @Getter @Setter private Integer vcpus = null;

        /**
         * 虚拟机最大内存数
         */
        @JSONField(name="max_mem")
        @Getter @Setter private Long maxMem = null;

        /**
         * 通过 cgroup 获取到的虚拟机 cpu user usage
         */
        @JSONField(name="cgroup_cpu_usage_user")
        @Getter @Setter private double cgroupCpuUsageUser;

        private final List<Double> cgroupCpuUsageUserHistory  =
                new ArrayList<>();

        private int getCgroupCpuUsageUserHistoryMax(){
            return 10;
        }

        @JSONField(name="cgroup_cpu_usage_user_ave")
        @Setter private double cgroupCpuUsageUserAve = -1.0;

        public double getCgroupCpuUsageUserAve(){
            double sum = 0;
            for(Double i : this.cgroupCpuUsageUserHistory){
                sum += i;
            }
            this.setCgroupCpuUsageUserAve(
                    sum / this.cgroupCpuUsageUserHistory.size());
            return this.cgroupCpuUsageUserAve;
        }

        /**
         * 通过 cgroup 获取到的虚拟机 cpu system usage
         */
        @JSONField(name="cgroup_cpu_usage_system")
        @Getter @Setter private double cgroupCpuUsageSystem;

        private final List<Double> cgroupCpuUsageSystemHistory =
                new ArrayList<>();

        private int getCgroupCpuUsageSystemHistoryMax(){
            return 10;
        }

        @JSONField(name="cgroup_cpu_usage_system_ave")
        @Setter private double cgroupCpuUsageSystemAve = -1.0;

        public double getCgroupCpuUsageSystemAve(){
            double sum = 0;
            for(Double i : this.cgroupCpuUsageSystemHistory){
                sum += i;
            }
            this.setCgroupCpuUsageSystemAve(
                    sum / this.cgroupCpuUsageSystemHistory.size());
            return this.cgroupCpuUsageSystemAve;
        }

        /**
         * 更新时间戳<br/>
         * 每次更新此类中的任意数据都会刷新此时间戳
         */
        @JSONField(name="timestamp")
        @Getter @Setter private long timestamp = 0;

        /**
         * 目前支持的指标：<br/>
         * hostname<br/>
         * vm.name<br/>
         * vm.vcpus<br/>
         * vm.max_mem<br/>
         * cgroup_cpu_usage_user<br/>
         * cgroup_cpu_system_user<br/>
         * @param metricName 指标名称
         * @param value 指标值
         */
        public void update(String metricName,
                           Object value){
            switch(metricName){
                case "hostname":
                    if(value.getClass() == String.class){
                        this.setHostname((String) value);
                    }
                    this.setTimestamp(CommonUtils.getTimestamp());
                    break;
                case "vm.uuid":
                    if(value.getClass() == String.class){
                        this.setVmUUID((String) value);
                    }
                    this.setTimestamp(CommonUtils.getTimestamp());
                    break;
                case "vm.name":
                    if(value.getClass() == String.class){
                        this.setVmName((String) value);
                    }
                    this.setTimestamp(CommonUtils.getTimestamp());
                    break;
                case "vm.vcpus":
                    if(value.getClass() == Integer.class){
                        this.setVcpus((Integer) value);
                    }
                    this.setTimestamp(CommonUtils.getTimestamp());
                    break;
                case "vm.max_mem":
                    if(value.getClass() == Long.class){
                        this.setMaxMem((Long) value);
                    }
                    this.setTimestamp(CommonUtils.getTimestamp());
                    break;
                case "cgroup_cpu_usage_user":
                    if(value.getClass() == Double.class){
                        this.setCgroupCpuUsageUser((Double) value);
                    }
                    this.setTimestamp(CommonUtils.getTimestamp());
                    if(this.cgroupCpuUsageUserHistory.size() >= this.getCgroupCpuUsageUserHistoryMax()){
                        this.cgroupCpuUsageUserHistory.remove(0);
                    }
                    this.cgroupCpuUsageUserHistory.add(this.getCgroupCpuUsageUser());
                    break;
                case "cgroup_cpu_usage_system":
                    if(value.getClass() == Double.class){
                        this.setCgroupCpuUsageSystem((Double) value);
                    }
                    this.setTimestamp(CommonUtils.getTimestamp());
                    if(this.cgroupCpuUsageSystemHistory.size() >= this.getCgroupCpuUsageSystemHistoryMax()){
                        this.cgroupCpuUsageSystemHistory.remove(0);
                    }
                    this.cgroupCpuUsageSystemHistory.add(this.getCgroupCpuUsageUser());
            }
        }
    }

    // hostname -> vm uuid -> VM instance
    @Getter private final Map<String, VM> vmMap =
            new ConcurrentHashMap<>();

    public void report(String vmUUID,
                       String metricName,
                       Object value){
        try{
            VM vm = getVmMap().get(vmUUID);
            vm.update(metricName, value);
        } catch (NullPointerException e){
            VM _vm = new VM();
            getVmMap().put(vmUUID, _vm);
            _vm.update(metricName, value);
        }
    }

    public String getAll(){
        JSONObject ret = new JSONObject();
        for(String uuid : getVmMap().keySet()){
            try{
                ret.put(uuid, getVmMap().get(uuid));
            } catch (NullPointerException ignored){

            }
        }
        return ret.toJSONString();
    }

    public List<VM> getVMByHostname(List<String> hostnames){
        List<VM> vmList = new ArrayList<>();

        for(String vmUUID : vmMap.keySet()){
            VM vm = vmMap.get(vmUUID);
            for(String hostname : hostnames){
                if(vm.getHostname().equals(hostname)){
                    vmList.add(vm);
                }
            }
        }

        return vmList;
    }
}
