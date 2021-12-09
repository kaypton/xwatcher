package com.github.fenrir.xcommon.utils;

import java.io.*;
import java.util.Arrays;

public class FileUtils {
    static public String readAll(String filePath) {

        StringBuilder sb = new StringBuilder();
        byte[] buffer = new byte[1024];
        Arrays.fill(buffer, (byte) 0);

        File file = new File(filePath);

        try (InputStream inputStream = new FileInputStream(file)) {
            while(inputStream.read(buffer) != -1){
                sb.append(new String(buffer));
                Arrays.fill(buffer, (byte) 0);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return sb.toString();
    }
}
