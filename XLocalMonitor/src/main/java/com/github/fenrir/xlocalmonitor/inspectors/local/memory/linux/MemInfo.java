package com.github.fenrir.xlocalmonitor.inspectors.local.memory.linux;

import com.github.fenrir.xcommon.utils.FileUtils;
import com.github.fenrir.xcommon.utils.Tuple2;
import com.github.fenrir.xlocalmonitor.annotations.Inspector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Inspector(name = "linux.meminfo")
public class MemInfo {

    private final Map<String, List<String>> data;
    private final Object dataLock = new Object();

    public MemInfo(){
        data = new ConcurrentHashMap<>();
        this.refresh();
    }

    public void refresh(){
        synchronized (this.dataLock) {
            this.data.clear();

            String meminfoContent = FileUtils.readAll("/proc/meminfo");
            if(meminfoContent != null){
                String[] splited = meminfoContent.split("\n");
                for(String item : splited){
                    String[] s = item.split("\\s+");
                    List<String> value = new ArrayList<>();

                    if(s.length == 2){
                        value.add(s[1]);
                    } else if(s.length == 3){
                        value.add(s[1]);
                        value.add(s[2]);
                    } else continue;

                    data.put(s[0].split(":")[0], value);
                }
            }
        }
    }

    public Tuple2<Long, Unit> getValue(String name){
        synchronized (this.dataLock) {
            if(this.data.containsKey(name)){
                return new Tuple2<>(Long.valueOf(this.data.get(name).get(0)),
                        this.data.size() == 1 ? null : Unit.from(this.data.get(name).get(1)));
            }else return null;
        }
    }

    public enum Unit {
        KiB;

        static public Unit from(String str){
            if(str.equals("kB"))
                return KiB;
            else return null;
        }
    }
}
