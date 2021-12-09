package com.github.fenrir.xlocalmonitor.inspectors.local.cpu.linux.proc;

import com.github.fenrir.xcommon.utils.FileUtils;
import com.github.fenrir.xlocalmonitor.annotations.Inspector;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Inspector(name = "linux.stat")
public class Stat {

    public static class StatData {
        private final Map<String, Map<String, Long>> cpuTime = new HashMap<>();

        public StatData(String statFileContent){
            String[] lines = statFileContent.split("\n");
            for(String line : lines){
                String[] words = line.split("\\s+");
                if(words[0].substring(0, 3).equals("cpu")){
                    Map<String, Long> _tmp = new HashMap<>();
                    _tmp.put("user", Long.valueOf(words[1]));
                    _tmp.put("nice", Long.valueOf(words[2]));
                    _tmp.put("system", Long.valueOf(words[3]));
                    _tmp.put("idle", Long.valueOf(words[4]));
                    _tmp.put("iowait", Long.valueOf(words[5]));
                    _tmp.put("irq", Long.valueOf(words[6]));
                    _tmp.put("softirq", Long.valueOf(words[7]));
                    _tmp.put("steal", Long.valueOf(words[8]));
                    _tmp.put("guest", Long.valueOf(words[9]));
                    _tmp.put("guest_nice", Long.valueOf(words[10]));
                    this.cpuTime.put(words[0], _tmp);
                }
            }
        }

        public Long getCpuUserTime(){
            return this.cpuTime.get("cpu").get("user");
        }

        public Long getCpuSystemTime(){
            return this.cpuTime.get("cpu").get("system");
        }

        public Long getCpuIdleTime(){
            return this.cpuTime.get("cpu").get("idle");
        }

        public Long getPerCpuUserTime(int index){
            return this.cpuTime.get("cpu" + index).get("user");
        }

        public Long getPerCpuSystemTime(int index){
            return this.cpuTime.get("cpu" + index).get("system");
        }

        public Long getPerCpuIdleTime(int index){
            return this.cpuTime.get("cpu" + index).get("idle");
        }

        public int getCoreNum(){
            return this.cpuTime.keySet().size() - 1;
        }

        public Long getTotalTime(){
            return this.cpuTime.get("cpu").get("user") +
                    this.cpuTime.get("cpu").get("system") +
                    this.cpuTime.get("cpu").get("nice") +
                    this.cpuTime.get("cpu").get("idle") +
                    this.cpuTime.get("cpu").get("iowait") +
                    this.cpuTime.get("cpu").get("irq") +
                    this.cpuTime.get("cpu").get("softirq") +
                    this.cpuTime.get("cpu").get("steal") +
                    this.cpuTime.get("cpu").get("guest") +
                    this.cpuTime.get("cpu").get("guest_nice");
        }
    }

    public Stat(){

    }

    public StatData get(){
        String statFileContent = FileUtils.readAll("/proc/stat");
        if(statFileContent != null){
            return new StatData(statFileContent);
        }
        return null;
    }

}
