package com.github.fenrir.xsniffer.exporter.file;

import com.alibaba.fastjson.JSONObject;
import com.github.fenrir.xmessaging.XMessage;
import com.github.fenrir.xsniffer.exporter.Exporter;
import com.github.fenrir.xsniffer.exporter.ExporterFactory;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.Map;

public class FileExporter implements Exporter {
    private static final Logger logger = LoggerFactory.getLogger("FileExporter");

    /**
     * file output format
     */
    @Getter @Setter private ExporterFactory.OutputFormat outputFormat = null;

    /**
     * output file name
     */
    @Getter @Setter private String filename = null;

    /**
     * file instance
     */
    @Getter @Setter private File file = null;

    /**
     * file writer
     */
    @Getter @Setter private Writer fileWriter = null;

    /**
     * first create the file
     */
    @Getter @Setter private Boolean firstCreate = true;

    public FileExporter(ExporterFactory.OutputFormat outputFormat,
                        Map<String, Object> extraParam){
        this.setOutputFormat(outputFormat);
        String filename = (String) extraParam.get("filename");
        if(filename == null){
            logger.error("filename is null");
            // TODO raise exception
            return;
        }
        this.setFile(new File(filename));

        try {
            this.setFileWriter(new FileWriter(this.getFile()));
        } catch (IOException e) {
            e.printStackTrace();
            this.setFileWriter(null);
        }
    }

    public void export(XMessage msg){
        if(this.getOutputFormat() == ExporterFactory.OutputFormat.JSON){
            try {
                this.getFileWriter().write(msg.getJSONObjectFromData().toJSONString());
                this.getFileWriter().write("\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else if(this.getOutputFormat() == ExporterFactory.OutputFormat.CSV){
            StringBuilder outputStringBuilder = new StringBuilder();
            JSONObject data = msg.getJSONObjectFromData();
            if(this.getFirstCreate()){
                this.setFirstCreate(false);
                StringBuilder titles = new StringBuilder();
                Iterator<String> iter = data.keySet().iterator();
                while(iter.hasNext()){
                    titles.append(iter.next());
                    if(iter.hasNext())
                        titles.append(",");
                }
                titles.append("\n");
                try {
                    this.getFileWriter().write(titles.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            Iterator<String> keys = data.keySet().iterator();
            while(keys.hasNext()){
                outputStringBuilder.append(data.getString(keys.next()));
                if(keys.hasNext())
                    outputStringBuilder.append(",");
            }
            outputStringBuilder.append("\n");
            try {
                this.getFileWriter().write(outputStringBuilder.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            this.getFileWriter().flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
