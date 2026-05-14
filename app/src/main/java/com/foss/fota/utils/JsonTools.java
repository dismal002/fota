package com.foss.fota.utils;

import android.text.TextUtils;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import java.util.List;

public class JsonTools {
    private static Gson a;

    private static Gson a() {
        if (a == null) {
            a = new Gson();
        }
        return a;
    }

    public static <T> T a(String str, Class<T> cls) {
        if (TextUtils.isEmpty(str)) {
            return null;
        }
        try {
            return (T) a().fromJson(str, (Class) cls);
        } catch (JsonSyntaxException e) {
            return null;
        } catch (JsonParseException e2) {
            return null;
        } catch (Exception e3) {
            return null;
        }
    }

    // Readable alias
    public static <T> T fromJson(String json, Class<T> cls) {
        return a(json, cls);
    }

    public static <T> String a(List<T> list) {
        return a().toJson(list);
    }

    public static <T> String a(T t) {
        return a().toJson(t);
    }

    public static <T> String toJson(T t) {
        return a(t);
    }
}
