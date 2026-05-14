package com.foss.fota.utils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.apache.commons.compress.utils.CharsetNames;

/* JADX INFO: compiled from: MD5.java */
/* JADX INFO: loaded from: classes.dex */
public final class MD5 {
    private static final char[] a = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

    public static String a(byte[] bArr) {
        if (bArr == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(bArr.length * 2);
        for (byte b : bArr) {
            sb.append(a[(b >> 4) & 15]);
            sb.append(a[b & 15]);
        }
        return sb.toString();
    }

    public static String a(String str) {
        try {
            return a(MessageDigest.getInstance("MD5").digest(str.getBytes(CharsetNames.UTF_8)));
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {
            return null;
        }
    }
    public static String getFileMD5(java.io.File file) {
        if (!file.exists()) return "";
        try (java.io.FileInputStream fis = new java.io.FileInputStream(file)) {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[8192];
            int len;
            while ((len = fis.read(buffer)) != -1) {
                digest.update(buffer, 0, len);
            }
            return a(digest.digest());
        } catch (Exception e) {
            return "";
        }
    }
}
