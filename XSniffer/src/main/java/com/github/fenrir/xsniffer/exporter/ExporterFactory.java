package com.github.fenrir.xsniffer.exporter;

import com.github.fenrir.xsniffer.exporter.file.FileExporter;
import lombok.Getter;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ExporterFactory {
    public enum Output {
        FILE,
        UDP,
        TCP
    }

    public enum OutputFormat {
        JSON,
        CSV
    }

    @Getter static final private Map<Output, Class<? extends Exporter>> exporterMap =
            new ConcurrentHashMap<>();

    static {
        getExporterMap().put(Output.FILE, FileExporter.class);
    }

    public static Exporter create(Output output, OutputFormat outputFormat, Map<String, Object> extraParam){
        try {
            return getExporterMap().get(output).getDeclaredConstructor(OutputFormat.class, Map.class)
                    .newInstance(outputFormat, extraParam);
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return null;
        }
    }
}
