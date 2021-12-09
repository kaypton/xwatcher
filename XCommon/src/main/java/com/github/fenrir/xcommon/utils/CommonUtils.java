package com.github.fenrir.xcommon.utils;

import java.util.Collection;
import java.util.UUID;

public class CommonUtils {
    public static String getUnusedUUID(Collection<String> usedUUID){
        String uuid = null;
        do{
            uuid = UUID.randomUUID().toString();
        }while(usedUUID.contains(uuid));
        return uuid;
    }

    public static long getTimestamp(){
        return System.currentTimeMillis()/1000;
    }

    public static long getTimestampMs(){
        return System.currentTimeMillis();
    }

    public static boolean ipAddressIsValid(String ipAddress){
        return ipAddress.matches("^(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[1-9])\\."
                + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."
                + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."
                + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)$");
    }
}
