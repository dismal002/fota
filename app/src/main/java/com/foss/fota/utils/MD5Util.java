package com.foss.fota.utils;

import java.security.MessageDigest;
import org.apache.commons.compress.utils.CharsetNames;

/* JADX INFO: compiled from: MD5Util.java */
/* JADX INFO: loaded from: classes.dex */
public class MD5Util {
    public static String a(String str) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update(str.getBytes(CharsetNames.UTF_8));
            return a(messageDigest.digest());
        } catch (Exception e) {
            return "";
        }
    }

    public static String a(byte[] bArr) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bArr) {
            String hexString = Integer.toHexString(b & 255);
            if (hexString.length() == 1) {
                sb.append("0").append(hexString);
            } else {
                sb.append(hexString);
            }
        }
        return sb.toString();
    }
}
