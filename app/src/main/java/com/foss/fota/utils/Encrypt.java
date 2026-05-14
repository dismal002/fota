package com.foss.fota.utils;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

/* JADX INFO: compiled from: Encrypt.java */
/* JADX INFO: loaded from: classes.dex */
public class Encrypt {
    private static Encrypt instance = null;

    private Encrypt() {
    }

    public static Encrypt getInstance() {
        if (instance == null) {
            instance = new Encrypt();
        }
        return instance;
    }

    // Compatibility alias from older sources.
    public static Encrypt getType() { return getInstance(); }

    public static String a(byte[] bArr) {
        String str = "";
        for (byte b : bArr) {
            String hexString = Integer.toHexString(b & 255);
            if (hexString.length() == 1) {
                str = str + "0" + hexString;
            } else {
                str = str + hexString;
            }
        }
        return str.toUpperCase();
    }

    public String a(String str) {
        byte[] bytes = str.getBytes();
        int iRandom = (int) (Math.random() * 15.0d);
        int iRandom2 = ((int) (Math.random() * 12.0d)) + 3;
        byte[] bArrA = a(iRandom2);
        int i = iRandom2 | (iRandom << 4);
        byte[] bArrA2 = a(bArrA, bytes);
        byte[] bArrB = b(bArrA);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
        try {
            dataOutputStream.writeByte(i);
            if (iRandom > 0) {
                byte[] bArr = new byte[iRandom];
                bArr[0] = 8;
                dataOutputStream.write(bArr);
            }
            dataOutputStream.write(bArrB);
            dataOutputStream.write(bArrA2);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            dataOutputStream.close();
            byteArrayOutputStream.close();
            return a(byteArray);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String encrypt(String str) {
        return a(str);
    }

    private byte[] a(int i) {
        byte[] bArr = new byte[i];
        for (int i2 = 0; i2 < i; i2++) {
            bArr[i2] = (byte) (Math.random() * 255.0d);
        }
        return bArr;
    }

    private byte[] b(byte[] bArr) {
        int length = bArr.length;
        byte[] bArr2 = new byte[length];
        for (int i = 0; i < length; i++) {
            bArr2[i] = (byte) (((bArr[i] & 255) >> 5) | ((bArr[i] & 255) << 3));
        }
        return bArr2;
    }

    private byte[] a(byte[] bArr, byte[] bArr2) {
        int length = bArr2.length;
        int length2 = bArr.length;
        byte[] bArr3 = new byte[length];
        int i = 0;
        for (int i2 = 0; i2 < length; i2++) {
            bArr3[i2] = (byte) ((bArr2[i2] & 255) ^ (bArr[i] & 255));
            i++;
            if (i == length2) {
                i = 0;
            }
        }
        return bArr3;
    }
}
