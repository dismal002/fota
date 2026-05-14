package com.foss.fota.utils;

import java.util.Stack;

/* JADX INFO: compiled from: ActivityStackUtil.java */
/* JADX INFO: loaded from: classes.dex */
public class ActivityStackUtil {
    private static Stack a = new Stack();
    private static boolean b = false;

    public static boolean a() {
        return b;
    }

    // Readable alias
    public static boolean isAppInForeground() {
        return a();
    }

    // Compatibility alias
    public static boolean getType() {
        return a();
    }

    public static void a(boolean z) {
        b = z;
    }
}
