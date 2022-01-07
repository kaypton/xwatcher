package com.github.fenrir.xdataflowtrace.entity;

public class InterfaceWithCount {
    public long count;
    public String interfaceName;
    public long startMs;
    public long stopMs;

    public InterfaceWithCount(){}

    public InterfaceWithCount(String word){
        this.interfaceName = word;
        this.count = 1;
    }

    public String toString(){
        return "InterfaceWithCount{interface=" +
                this.interfaceName + ",count=" + this.count + ",start=" + this.startMs +
                ",stop=" + this.stopMs + "}";
    }
}
