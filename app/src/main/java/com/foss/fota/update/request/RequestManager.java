package com.foss.fota.update.request;

import android.content.Context;
import com.foss.fota.utils.Encrypt;
import java.util.HashMap;
import java.util.Map;

/* JADX INFO: compiled from: RequestManager.java */
/* JADX INFO: loaded from: classes.dex */
public class RequestManager {
    private static RequestManager instance;
    private Context mContext;

    private RequestManager(Context context) {
        this.mContext = context.getApplicationContext();
    }

    public static synchronized RequestManager getInstance(Context context) {
        if (instance == null) {
            instance = new RequestManager(context);
        }
        return instance;
    }

    public void query() {
        executeRequest(mContext, null);
    }

    public static void executeRequest(Context context, RequestBase.RequestCallback callback) {
        sendEncryptedRequest(com.foss.fota.config.ServerApi.LEGACY_QUERY_URL, RequestParam.getParamMap(context), callback);
    }

    private static void sendEncryptedRequest(String url, Map<String, String> params, RequestBase.RequestCallback callback) {
        StringBuilder sb = new StringBuilder();
        if (params != null) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                if (sb.length() > 0) {
                    sb.append("&");
                }
                sb.append(entry.getKey()).append("=").append(entry.getValue());
            }
        }
        HashMap<String, String> encryptedMap = new HashMap<>();
        encryptedMap.put("key", Encrypt.getInstance().encrypt(sb.toString()));
        new RequestBase(url).executeAsync(encryptedMap, callback);
    }
}
