package com.foss.fota.utils;

/* JADX INFO: compiled from: StringUtils.java */
/* JADX INFO: loaded from: classes.dex */
public class StringUtils {
    public static long a(String str) {
        byte[] bytes = str.getBytes();
        if (0 < bytes.length) {
            return bytes[0] - 96;
        }
        return 0L;
    }

    public static boolean a(char c) {
        return c >= '0' && c <= '9';
    }

    public static String b(String str) {
        char[] charArray = str.toCharArray();
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < charArray.length; i++) {
            if (a(charArray[i])) {
                stringBuffer.append(charArray[i]);
            } else {
                stringBuffer.append(a(String.valueOf(charArray[i]).toLowerCase()));
            }
        }
        return stringBuffer.toString();
    }

    public static String stripNonDigits(String str) {
        return b(str);
    }
}
