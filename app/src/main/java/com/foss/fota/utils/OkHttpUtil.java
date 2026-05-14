package com.foss.fota.utils;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import java.io.IOException;
import java.security.Security;
import java.util.concurrent.TimeUnit;

public class OkHttpUtil {
    private static final OkHttpClient client = new OkHttpClient();

    static {
        client.setConnectTimeout(15L, TimeUnit.SECONDS);
        client.setReadTimeout(25L, TimeUnit.SECONDS);
        client.setWriteTimeout(25L, TimeUnit.SECONDS);
        client.setFollowRedirects(true);
        client.setFollowSslRedirects(true);
        client.setHostnameVerifier(AduHostnameVerifier.INSTANCE);
    }

    public static Response execute(Request request) throws IOException {
        Response response = client.newCall(request).execute();
        if (response != null && response.isSuccessful()) {
            return response;
        }
        throw new IOException("Unexpected code " + response);
    }

    public static Response execute(Request request, com.foss.fota.update.request.RequestResult result) throws IOException {
        Response response = client.newCall(request).execute();
        if (response != null && response.isSuccessful()) {
            return response;
        }
        if (response != null) {
            result.setHttpCode(response.code());
        }
        throw new IOException("Unexpected code " + response);
    }

    public static void enqueue(Request request, Callback callback) {
        client.newCall(request).enqueue(callback);
    }

    public static void clearDnsCache() {
        Security.setProperty("networkaddress.cache.ttl", String.valueOf(0));
        Security.setProperty("networkaddress.cache.negative.ttl", String.valueOf(0));
    }

    // Compatibility aliases
    public static void a() { clearDnsCache(); }
    public static void getType() { clearDnsCache(); } // Fix for previous script error
    public static Response a(Request request) throws IOException { return execute(request); }
}
