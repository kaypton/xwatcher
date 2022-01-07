package com.github.fenrir.xdataflowtrace;

import com.alibaba.fastjson.JSONObject;
import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.WindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.SlidingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.fenrir.xdataflowtrace.entity.InterfaceWithCount;

import java.time.Duration;

public class XDataFlowTraceApplication {

    private final static Logger LOGGER = LoggerFactory.getLogger(XDataFlowTraceApplication.class);

    static public void main(String[] args){
        ParameterTool parameterTool = ParameterTool.fromArgs(args);

        int sourceParallelism = parameterTool.getInt("sourceParallelism", 1);
        int mapParallelism = parameterTool.getInt("mapParallelism", 1);
        String natsServerAddresses = parameterTool.get("natsServerAddresses", "");
        String natsSubject = parameterTool.get("natsSubject", "");
        long watermarkIdlenessSec = parameterTool.getLong("watermarkIdlenessSec", -1);

        if(natsSubject.equals("") || natsServerAddresses.equals("")){
            LOGGER.error("nats subject or nats server address has not been set");
            return;
        }

        if(watermarkIdlenessSec == -1){
            LOGGER.error("watermark idleness(second) has not been set");
        }

        // TODO idleness should smaller than window size

        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        DataStreamSource<String> source =
                env.addSource(new NatsParallelSourceFunction(natsServerAddresses,
                        natsSubject), "Nats Source").setParallelism(sourceParallelism);
        WatermarkStrategy<String> watermarkStrategy = WatermarkStrategy
                .<String>forBoundedOutOfOrderness(Duration.ofSeconds(1))
                .withTimestampAssigner(new SerializableTimestampAssigner<String>() {
                    @Override
                    public long extractTimestamp(String element, long recordTimestamp) {
                        return JSONObject.parseObject(element)
                                .getJSONArray("resourceSpans").getJSONObject(0)
                                .getJSONArray("instrumentationLibrarySpans").getJSONObject(0)
                                .getJSONArray("spans").getJSONObject(0)
                                .getLongValue("startTimeUnixNano") / 1000000;
                    }
                })
                .withIdleness(Duration.ofSeconds(watermarkIdlenessSec));

        DataStream<InterfaceWithCount> mapStream = source
                .assignTimestampsAndWatermarks(watermarkStrategy)
                .flatMap(new FlatMapFunction<String, InterfaceWithCount>() {
                    @Override
                    public void flatMap(String value, Collector<InterfaceWithCount> out) {
                        JSONObject uri = JSONObject.parseObject(value);
                        String interfaceName = uri
                                .getJSONArray("resourceSpans").getJSONObject(0)
                                .getJSONArray("instrumentationLibrarySpans").getJSONObject(0)
                                .getJSONArray("spans").getJSONObject(0)
                                .getString("name");

                        out.collect(new InterfaceWithCount(interfaceName));
                    }
                }).setParallelism(mapParallelism);

        KeyedStream<InterfaceWithCount, String> keyByStream = mapStream.keyBy(new KeySelector<InterfaceWithCount, String>() {
            @Override
            public String getKey(InterfaceWithCount value) {
                return value.interfaceName;
            }
        });

        SingleOutputStreamOperator<InterfaceWithCount> sumStream = keyByStream
                .window(SlidingEventTimeWindows.of(Time.seconds(5), Time.seconds(1)))
                .apply(new WindowFunction<InterfaceWithCount, InterfaceWithCount, String, TimeWindow>() {
                    @Override
                    public void apply(String s, TimeWindow window, Iterable<InterfaceWithCount> input, Collector<InterfaceWithCount> out) throws Exception {
                        InterfaceWithCount interfaceWithCount = new InterfaceWithCount(s);
                        for(InterfaceWithCount i : input){
                            interfaceWithCount.count += 1;
                        }
                        interfaceWithCount.count -= 1;
                        interfaceWithCount.startMs = window.getStart();
                        interfaceWithCount.stopMs = window.getEnd();
                        System.out.println("window : [" + window.getStart() + ", " + window.getEnd() + "]");;
                        out.collect(interfaceWithCount);
                    }
                });
        sumStream.print().setParallelism(4);

        try {
            env.execute("TraceDataFlow");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
