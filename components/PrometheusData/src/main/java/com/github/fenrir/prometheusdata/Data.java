package com.github.fenrir.prometheusdata;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Data {
    private final String name;
    private final Map<String, String> labels = new ConcurrentHashMap<>();;
    private final String help;
    private final String id;
    private final  GetDataMethod getDataMethod;
    final private MetricType metricType;

    private String[] extension = null;

    public Data(String name,
                GetDataMethod getDataMethod,
                String help,
                MetricType metricType,
                String id){
        this.name = name;
        this.getDataMethod = getDataMethod;
        this.metricType = metricType;
        this.help = help;

        this.id = id;
    }

    public Data(String name,
                GetDataMethod getDataMethod,
                String help,
                MetricType metricType,
                String id,
                String... extension){
        this.name = name;
        this.getDataMethod = getDataMethod;
        this.metricType = metricType;
        this.help = help;

        this.extension = extension;
        this.id = id;
    }

    public String getMetricPlainTextString(){
        StringBuilder sb = new StringBuilder();

        if(this.metricType != MetricType.HISTOGRAM){
            if(this.help != null){
                sb.append("# HELP ").append(this.help).append("\n");
                sb.append("# TYPE ").append(this.name).append(" ").append(metricType.toString()).append("\n");
                sb.append(this.name).append("{");
                int labelNum = this.labels.keySet().size();
                int count = 0;
                for(String key : this.labels.keySet()){
                    sb.append(key).append("=\"").append(this.labels.get(key)).append("\"");
                    count += 1;
                    if(count != labelNum)
                        sb.append(",");
                }
                sb.append("} ");
                Double data;
                if(this.extension == null)
                    data = this.getDataMethod.getData(this.name);
                else data = this.getDataMethod.getData(this.name, extension);
                if(data != null){
                    sb.append(new BigDecimal(Double.toString(data)).toPlainString()).append("\n");
                }else{
                    return null;
                }
            }
        }
        return sb.toString();
    }

    public void addLabel(String key, String value){
        if(value == null) return;
        this.labels.put(key, value);
    }

    public String getName(){
        return this.name;
    }

    public String getId(){
        return this.id;
    }
}
