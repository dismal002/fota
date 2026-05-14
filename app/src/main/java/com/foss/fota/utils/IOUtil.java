package com.foss.fota.utils;

import java.io.Closeable;

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
