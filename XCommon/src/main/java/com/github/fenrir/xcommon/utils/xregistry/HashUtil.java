package com.github.fenrir.xcommon.utils.xregistry;

import com.github.fenrir.xcommon.clients.xlocalmonitor.types.LocalMonitorType;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashUtil {
    static public String getLocalMonitorSHA256Hash(String hostname,
                                                   LocalMonitorType localMonitorType,
                                                   String ipAddr){
        StringBuilder sb = new StringBuilder();
        sb.append(hostname).append("#").append(localMonitorType.toString()).append("#")
                .append(ipAddr);

        MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(sb.toString().getBytes(StandardCharsets.UTF_8));
            return byte2Hex(messageDigest.digest());
        } catch (NoSuchAlgorithmException e){
            e.printStackTrace();
            return null;
        }
    }

    private static String byte2Hex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        String temp;
        for (byte aByte : bytes) {
            temp = Integer.toHexString(aByte & 0xFF);
            if (temp.length() == 1) {
                sb.append("0");
            }
            sb.append(temp);
        }
        return sb.toString();
    }
}
