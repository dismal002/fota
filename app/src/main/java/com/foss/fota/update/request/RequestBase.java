package com.foss.fota.update.request;

import com.foss.fota.utils.Trace;
import com.foss.fota.utils.OkHttpUtil;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/* JADX INFO: compiled from: RequestBase.java */
/* JADX INFO: loaded from: classes.dex */
public class RequestBase {
    private String url;

    public interface RequestCallback {
        void onSuccess(String content) throws Exception;
    }

    public RequestBase(String url) {
        this.url = url;
    }

    public RequestResult executeSync(HashMap<String, String> params) {
        RequestResult result = new RequestResult();
        try {
            result.setStartTime(System.currentTimeMillis());
            FormEncodingBuilder builder = new FormEncodingBuilder();
            if (params != null) {
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    builder.add(entry.getKey(), entry.getValue());
                }
            }
            Request request = new Request.Builder()
                    .url(this.url)
                    .post(builder.build())
                    .build();
            
            Trace.d("http URL = " + this.url);
            Response response = null;
            try {
                response = OkHttpUtil.execute(request);
            } catch (IOException e) {
                Trace.d("http request failed: " + e.getMessage());
                return result.setSuccess(false)
                        .setErrorCode(3008)
                        .setErrorMessage(e.getMessage());
            }

            int code = response.code();
            Trace.d("http response code = " + code);
            
            result.setHttpCode(code);
            result.setEndTime(System.currentTimeMillis());
            
            if (response.isSuccessful()) {
                return result.setSuccess(true)
                        .setContent(response.body().string());
            } else {
                return result.setSuccess(false)
                        .setErrorCode(3010)
                        .setContent(response.body().string());
            }
        } catch (IOException e) {
            return result.setSuccess(false)
                    .setErrorCode(3008)
                    .setErrorMessage(e.getMessage());
        } catch (Exception e2) {
            return result.setSuccess(false)
                    .setErrorCode(3010)
                    .setErrorMessage(e2.getMessage());
        }
    }

    // Compatibility alias
    public RequestResult execute(HashMap<String, String> params) {
        return executeSync(params);
    }

    public void executeAsync(Map<String, String> params, final RequestCallback callback) {
        FormEncodingBuilder builder = new FormEncodingBuilder();
        if (params != null) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                builder.add(entry.getKey(), entry.getValue());
            }
        }
        Request request = new Request.Builder()
                .url(this.url)
                .post(builder.build())
                .build();
        
        Trace.d("http url = " + this.url);
        OkHttpUtil.enqueue(request, new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                Trace.d("http request fail, message: " + e.getMessage());
            }

            @Override
            public void onResponse(Response response) {
                Trace.d("http request success, response code = " + response.code());
                try {
                    if (response.isSuccessful()) {
                        if (callback != null) {
                            callback.onSuccess(response.body().string());
                        }
                    } else {
                        Trace.d("http request error, message: " + response.message());
                    }
                } catch (Exception e) {
                    Trace.d("http request exception, message: " + e.getMessage());
                }
            }
        });
    }
}
