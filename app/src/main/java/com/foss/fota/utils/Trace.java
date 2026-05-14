package com.foss.fota.utils;

import android.util.Log;
import com.foss.fota.utils.FileUtil;

public class Trace {
    public static boolean a = true;
    private static boolean b = true;
    private static char c = 'v';
    private static boolean d = true;
    private static String e = "";
    private static boolean f = true;
    private static boolean g = true;

    public static void d(String str, String str2) {
        log("FotaUpdate", formatMessage(str2), null, 'd');
    }

    public static void d(String str) {
        d("FotaUpdate", str);
    }

    private static String formatMessage(String str) {
        return str;
    }

    public static void debugIf(boolean z, String str) {
        String strD = formatMessage(str);
        if (z) {
            log("FotaUpdate", getCallerInfo(strD), null, 'd');
        }
        if (d) {
            FileUtil.a(e, "FotaUpdate: " + getCallerInfo(strD));
        }
    }

    public static void e(String str) {
        log("FotaUpdate", formatMessage(str), null, 'e');
    }

    public static void e(String str, String str2) {
        log("FotaUpdate", formatMessage(str2), null, 'e');
    }

    public static void e(String str, String str2, Throwable th) {
        log(str, formatMessage(str2), th, 'e');
    }

    private static void log(String str, String str2, Throwable th, char c2) {
        boolean loggingPref = g;
        if (com.foss.fota.MyApplication.getInstance() != null) {
            loggingPref = PreferencesUtils.getBoolean(com.foss.fota.MyApplication.getInstance(), "user_logging_enabled", g);
        }
        String strD = formatMessage(str2);
        if ((b || d) && loggingPref) {
            if ('e' == c2 && ('e' == c || 'v' == c)) {
                Log.e(str, getCallerInfo(strD), th);
            } else if ('w' == c2 && ('w' == c || 'v' == c)) {
                Log.w(str, getCallerInfo(strD), th);
            } else if ('d' == c2 && ('d' == c || 'v' == c)) {
                Log.d(str, getCallerInfo(strD), th);
            } else if ('i' == c2 && ('d' == c || 'v' == c)) {
                Log.i(str, getCallerInfo(strD), th);
            } else {
                Log.v(str, getCallerInfo(strD), th);
            }
            if (d) {
                FileUtil.a(e, str + ": " + getCallerInfo(strD));
            }
        }
    }

    private static String getStackTrace() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        if (stackTrace == null) {
            return null;
        }
        for (StackTraceElement stackTraceElement : stackTrace) {
            if (!stackTraceElement.isNativeMethod() && !stackTraceElement.getClassName().equals(Thread.class.getName()) && !stackTraceElement.getFileName().equals("Trace.java")) {
                return "[" + stackTraceElement.getFileName() + ":" + stackTraceElement.getLineNumber() + "] " + stackTraceElement.getMethodName();
            }
        }
        return null;
    }

    private static String getCallerInfo(String str) {
        String strD = formatMessage(str);
        String strB = getStackTrace();
        if (strB != null) {
            return strB + " -> " + strD;
        }
        return strD;
    }

    public static boolean isDebugEnabled() {
        return d;
    }

    public static void setDebugEnabled(boolean z) {
        d = z;
        a = z;
        f = z;
    }

    public static void setLogPath(String str) {
        e = str;
    }

    public static void setLoggingEnabled(boolean z) {
        g = z;
    }

    // Compatibility alias from the migration.
    public static void selectedFile(String tag, String message) {
        d(tag, message);
    }

    public static void a(String msg) {
        d(msg);
    }

    public static boolean isLogEnabled() {
        if (com.foss.fota.MyApplication.getInstance() != null) {
            return PreferencesUtils.getBoolean(com.foss.fota.MyApplication.getInstance(), "user_logging_enabled", g);
        }
        return g;
    }
}
