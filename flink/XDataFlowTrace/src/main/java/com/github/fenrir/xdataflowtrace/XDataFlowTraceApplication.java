package com.github.fenrir.xdataflowtrace;

import com.github.fenrir.xdataflowtrace.configs.URISelectorConfig;
import com.github.fenrir.xdataflowtrace.entity.trace.Span;
import com.github.fenrir.xdataflowtrace.functions.SpanFlatMapFunction;
import com.github.fenrir.xdataflowtrace.functions.SpanKeySelector;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.fenrir.xdataflowtrace.entity.SpanWithCount;

public class XDataFlowTraceApplication {

    private final static Logger LOGGER = LoggerFactory.getLogger(XDataFlowTraceApplication.class);

    static public void main(String[] args){
        ParameterTool parameterTool = ParameterTool.fromArgs(args);

        int sourceParallelism = parameterTool.getInt("sourceParallelism", 4);
        int mapParallelism = parameterTool.getInt("mapParallelism", 1);
        String natsServerAddresses = parameterTool.get("natsServerAddresses", "nats://222.201.144.237:4222,nats://222.201.144.237:4223,nats://222.201.144.237:4224");
        String natsSubject = parameterTool.get("natsSubject", "stream.trainticket.trace");
        long watermarkIdlenessSec = parameterTool.getLong("watermarkIdlenessSec", 1);

        if(natsSubject.equals("") || natsServerAddresses.equals("")){
            LOGGER.error("nats subject or nats server address has not been set");
            return;
        }

        if(watermarkIdlenessSec == -1){
            LOGGER.error("watermark idleness(second) has not been set");
        }

        // TODO idleness should smaller than window size

        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        DataStreamSource<Span> source = env.addSource(new NatsParallelSourceFunction(natsServerAddresses,
                        natsSubject), "Nats Source").setParallelism(sourceParallelism);

        /*WatermarkStrategy<String> watermarkStrategy = WatermarkStrategy
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
                .withIdleness(Duration.ofSeconds(watermarkIdlenessSec));*/

        DataStream<SpanWithCount> mapStream = source
                //.assignTimestampsAndWatermarks(watermarkStrategy)
                .flatMap(new SpanFlatMapFunction(URISelectorConfig.tmpCreate()))
                .setParallelism(mapParallelism);

        KeyedStream<SpanWithCount, String> keyedByServiceNameStream = mapStream
                .keyBy(new SpanKeySelector(SpanKeySelector.Key.SERVICE_NAME));
        KeyedStream<SpanWithCount, String> keyedByInterfaceURIStream = keyedByServiceNameStream
                .keyBy(new SpanKeySelector(SpanKeySelector.Key.INTERFACE_URI));

        keyedByInterfaceURIStream.print();

        /*SingleOutputStreamOperator<InterfaceWithCount> sumStream = keyByStream
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
        sumStream.print().setParallelism(4);*/

        try {
            env.execute("TraceDataFlow");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
