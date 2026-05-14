package com.foss.fota.utils;

import java.io.Closeable;

/* JADX INFO: compiled from: IOUtil.java */
/* JADX INFO: loaded from: classes.dex */
public class IOUtil {
    public static void a(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Throwable th) {
            }
        }
    }
}
