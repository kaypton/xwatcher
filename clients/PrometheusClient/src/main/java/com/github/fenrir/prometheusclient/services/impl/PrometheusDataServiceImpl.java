package com.github.fenrir.prometheusclient.services.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.fenrir.prometheusclient.entities.PrometheusQueryResponse;
import com.github.fenrir.prometheusclient.entities.PrometheusQueryResponseBuilder;
import com.github.fenrir.prometheusclient.entities.impl.PrometheusQueryResponseImpl;
import com.github.fenrir.prometheusclient.services.PrometheusDataService;
import com.github.fenrir.xcommon.utils.Tuple2;
import com.github.fenrir.xhttpclient.Client;
import com.github.fenrir.xhttpclient.XHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("PrometheusDataServiceDefault")
public class PrometheusDataServiceImpl implements PrometheusDataService {
    private static final Logger LOGGER = LoggerFactory.getLogger(PrometheusDataServiceImpl.class);

    private static final String QUERY_URI = "/api/v1/query";
    private static final String QUERY_RANGE_URI = "/api/v1/query_range";

    private Client httpClient = null;

    private ConfigurableApplicationContext context = null;

    @Override
    public void init(ConfigurableApplicationContext context){
        this.context = context;
    }

    private Client getXHttpClient(){
        if(this.httpClient == null){
            if(this.context == null){
                LOGGER.error("Spring Application Context is null, please init first");
                return null;
            }
            this.httpClient = this.context.getBean("PrometheusClientXHttpClient", Client.class);
        }
        return this.httpClient;
    }

    private void queryRange(){

    }

    public PrometheusQueryResponse query(String query, Long timestamp){
        PrometheusQueryResponseBuilder builder = PrometheusQueryResponse.newBuilder();

        Map<String, String> parameters = new HashMap<>();
        parameters.put("query", query);
        if(timestamp != null){
            parameters.put("time", timestamp.toString());
        }

        Client client = this.getXHttpClient();
        if(client != null){
            XHttpResponse<String> response = client.get(QUERY_URI, null, parameters, null, String.class);
            if(response.statusCode() == 200){ // get result successful
                this.parseResult(response.body(), builder);
            }

            return builder.build();
        }
        return null;
    }

    private void parseResult(@NotNull String resultString, PrometheusQueryResponseBuilder builder){
        JSONObject jsonObject = JSON.parseObject(resultString);

        JSONObject data = jsonObject.getJSONObject("data");
        String resultType = data.getString("resultType");

        JSONArray results = data.getJSONArray("result");

        for(int i = 0; i < results.size(); i++){
            JSONObject result = results.getJSONObject(i);
            JSONObject metric = result.getJSONObject("metric");

            String name = metric.getString("__name__");
            Map<String, String> labels = new HashMap<>();
            List<Tuple2<Double, Double>> values = new ArrayList<>();

            for(String key : metric.keySet()){
                if(!key.equals("__name__")){
                    labels.put(key, metric.getString(key));
                }
            }

            if(resultType.equals("vector")){
                JSONArray value = result.getJSONArray("value");
                values.add(new Tuple2<>(value.getDouble(1), value.getDouble(0)));
            }else if(resultType.equals("matrix")){
                JSONArray value = result.getJSONArray("values");
                for(int j = 0; j < value.size(); j++){
                    JSONArray v = value.getJSONArray(j);
                    values.add(new Tuple2<>(v.getDouble(1), v.getDouble(0)));
                }
            }

            builder.withResult(new PrometheusQueryResponseImpl.Result(name, labels, values));
        }
    }
}
