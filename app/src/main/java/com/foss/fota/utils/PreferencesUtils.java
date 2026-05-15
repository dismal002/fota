package com.foss.fota.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferencesUtils {
    private static final String PREF_NAME = "fota_prefs";

    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static void putString(Context context, String key, String value) {
        getPrefs(context).edit().putString(key, value).apply();
    }

    public static String getString(Context context, String key) {
        return getString(context, key, "");
    }

    public static String getString(Context context, String key, String defaultValue) {
        try {
            return getPrefs(context).getString(key, defaultValue);
        } catch (Exception e) {
            try {
                Object value = getPrefs(context).getAll().get(key);
                if (value != null) {
                    return String.valueOf(value);
                }
            } catch (Exception ignored) {
            }
            return defaultValue;
        }
    }

    public static void putInt(Context context, String key, int value) {
        getPrefs(context).edit().putInt(key, value).apply();
    }

    public static int getInt(Context context, String key, int defaultValue) {
        return getPrefs(context).getInt(key, defaultValue);
    }

    public static void putLong(Context context, String key, long value) {
        getPrefs(context).edit().putLong(key, value).apply();
    }

    public static long getLong(Context context, String key, long defaultValue) {
        return getPrefs(context).getLong(key, defaultValue);
    }

    public static void putBoolean(Context context, String key, boolean value) {
        getPrefs(context).edit().putBoolean(key, value).apply();
    }

    public static boolean getBoolean(Context context, String key, boolean defaultValue) {
        return getPrefs(context).getBoolean(key, defaultValue);
    }

    // Compatibility aliases
    public static int b(Context context, String key, int defaultValue) {
        return getInt(context, key, defaultValue);
    }

    public static int b(Context context, String key) {
        return getInt(context, key, 0);
    }

    public static String b(Context context, String key, String defaultValue) {
        return getString(context, key, defaultValue);
    }

    public static boolean b(Context context, String key, boolean defaultValue) {
        return getBoolean(context, key, defaultValue);
    }

    public static long c(Context context, String key) {
        return getLong(context, key, 0);
    }

    public static boolean d(Context context, String key) {
        return getBoolean(context, key, false);
    }

    public static void a(Context context, String key, int value) {
        putInt(context, key, value);
    }

    public static void a(Context context, String key, String value) {
        putString(context, key, value);
    }

    public static void a(Context context, String key, boolean value) {
        putBoolean(context, key, value);
    }

    public static long b(Context context, String key, long defaultValue) {
        return getLong(context, key, defaultValue);
    }

    public static void a(Context context, String key, long value) {
        putLong(context, key, value);
    }

    // Decompiler artifacts
    public static int contentLayout(Context context, String key, int defaultValue) {
        return getInt(context, key, defaultValue);
    }

    public static String contentLayout(Context context, String key, String defaultValue) {
        return getString(context, key, defaultValue);
    }

    public static boolean contentLayout(Context context, String key, boolean defaultValue) {
        return getBoolean(context, key, defaultValue);
    }

    public static void putInt(Context context, String key, long value) {
        putInt(context, key, (int) value);
    }

    public static void putInt(Context context, String key, String value) {
        try {
            putInt(context, key, Integer.parseInt(value));
        } catch (Exception e) {
            putInt(context, key, 0);
        }
    }

    public static void putBoolean(Context context, String key, long value) {
        putBoolean(context, key, value != 0);
    }

    public static void putBoolean(Context context, String key, String value) {
        putBoolean(context, key, Boolean.parseBoolean(value));
    }

    public static void a(Context context, String key, Object value) {
        if (value instanceof String) {
            putString(context, key, (String) value);
        } else if (value instanceof Integer) {
            putInt(context, key, (Integer) value);
        } else if (value instanceof Long) {
            putLong(context, key, (Long) value);
        } else if (value instanceof Boolean) {
            putBoolean(context, key, (Boolean) value);
        }
    }

    public static void putBoolean(Context context, boolean value) {
        /* mystery call in InstallParser1 */
    }
}
