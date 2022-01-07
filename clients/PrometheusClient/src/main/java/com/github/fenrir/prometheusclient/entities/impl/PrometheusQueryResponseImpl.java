package com.github.fenrir.prometheusclient.entities.impl;

import com.github.fenrir.prometheusclient.entities.PrometheusQueryResponse;
import com.github.fenrir.prometheusclient.entities.PrometheusQueryResponseBuilder;
import com.github.fenrir.prometheusclient.entities.PrometheusQueryResult;
import com.github.fenrir.xcommon.utils.Tuple2;

import javax.validation.constraints.NotNull;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PrometheusQueryResponseImpl implements PrometheusQueryResponse {

    private final List<PrometheusQueryResult> results = new ArrayList<>();

    private PrometheusQueryResponseImpl(){

    }

    private void addResult(PrometheusQueryResult result){
        this.results.add(result);
    }

    public int resultsNum(){
        return this.results.size();
    }

    public List<PrometheusQueryResult> getResults(){
        return this.results;
    }

    public static class Result implements PrometheusQueryResult {
        private final String name;
        private final Map<String, String> labels;

        // tuple2 <Value, Timestamp>
        private final List<Tuple2<Double, Double>> values;

        public Result(@NotNull String name,
                      Map<String, String> labels,
                      List<Tuple2<Double, Double>> values){
            this.name = name;
            this.labels = labels;
            this.values = values;
        }

        public int valueNum(){
            return this.values.size();
        }

        public String getName(){
            return this.name;
        }

        public Set<String> getLabelNames(){
            if(this.labels != null){
                return this.labels.keySet();
            }
            return null;
        }

        public String getLabel(String name){
            if(this.labels != null){
                return this.labels.getOrDefault(name, null);
            }
            return null;
        }

        public List<Tuple2<Double, Double>> getValues(){
            return this.values;
        }

        @Override
        public String toString(){
            StringBuilder sb = new StringBuilder();
            DecimalFormat df = new DecimalFormat("0.##");
            DecimalFormat df1 = new DecimalFormat("0.#######");

            sb.append("name:").append(this.name).append("\n");
            sb.append("labels:").append("\n");
            for(String key : this.labels.keySet()){
                sb.append("\t").append(key).append(":").append(this.labels.get(key)).append("\n");
            }
            for(Tuple2<Double, Double> value : this.values){
                sb.append("[ timestamp:").append(df.format(value.second)).append("\t")
                        .append("value:").append(df1.format(value.first)).append(" ]\n");
            }
            return sb.toString();
        }
    }

    public static class Builder implements PrometheusQueryResponseBuilder {

        private final PrometheusQueryResponseImpl response;

        public Builder(){
            this.response = new PrometheusQueryResponseImpl();
        }

        public PrometheusQueryResponseBuilder withResult(PrometheusQueryResult result){
            this.response.addResult(result);
            return this;
        }

        public PrometheusQueryResponse build(){
            return this.response;
        }
    }

    public static Builder newBuilder(){
        return new Builder();
    }
}
