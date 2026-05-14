package com.foss.fota.update.report;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import com.foss.fota.MyApplication;
import com.foss.fota.update.model.ReportModel;
import com.foss.fota.update.db.UpdateDBAdapter;
import com.foss.fota.utils.Trace;
import com.foss.fota.utils.DeviceInfoProvider;
import com.foss.fota.utils.DeviceUtil;
import com.foss.fota.utils.JsonTools;
import com.foss.fota.utils.OkHttpUtil;
import com.foss.fota.utils.PackageUtils;
import com.foss.fota.utils.PreferencesUtils;
import com.foss.fota.utils.StorageUtil;
import com.foss.fota.utils.Mid;
import com.foss.fota.utils.NetWorkUtil;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/* JADX INFO: compiled from: ReportManager.java */
/* JADX INFO: loaded from: classes.dex */
public class ReportManager {
    private static ExecutorService executor;
    private static Context context;
    private static ReportManager instance = null;

    public interface ReportCallback {
        void onSuccess();
        void onFailure();
    }

    private ReportManager(Context context) {
        this.context = context.getApplicationContext();
        executor = Executors.newSingleThreadExecutor();
    }

    public static ReportManager getInstance(Context context) {
        if (instance == null) {
            synchronized (ReportManager.class) {
                if (instance == null) {
                    instance = new ReportManager(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    public void scheduleReport() {
        if (!isReportScheduled("scheduleReportData", 7200000L)) {
            Trace.d("not arrive report data schedule!");
        } else {
            submitQueuedReports();
        }
    }

    public void submitQueuedReports() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                ReportManager.this.performReportTask(ReportManager.context);
            }
        });
    }

    public void report(final String type, final String content, final ReportCallback callback) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                Trace.d("insert data to db, type = " + type + "; result = " + content);
                boolean success = UpdateDBAdapter.getInstance(context).insertReport(type, content);
                if (callback != null) {
                    if (success) {
                        callback.onSuccess();
                    } else {
                        callback.onFailure();
                    }
                }
            }
        });
    }

    public void report(final String type, final String content) {
        report(type, content, null);
    }

    public void performReportTask(Context context) {
        try {
            List<ReportModel.ReportResult> reports = UpdateDBAdapter.getInstance(context).queryReports(100);
            if (reports != null && reports.size() > 0) {
                if (!MyApplication.isConnectNetAllowed()) {
                    Trace.d("Privacy: report blocked because connect_net is false");
                    return;
                }
                Trace.d("record items size= " + reports.size());
                ArrayList<String> resultList = new ArrayList<>();
                for (ReportModel.ReportResult report : reports) {
                    resultList.add(report.result);
                }
                
                String baseUrl = PreferencesUtils.getString(context, "check_url", com.foss.fota.config.ServerApi.PRIMARY_DOMAIN);
                String url = baseUrl + com.foss.fota.config.ServerApi.REPORT_ENDPOINT;
                
                MultipartBuilder builder = new MultipartBuilder();
                builder.addFormDataPart("project", DeviceUtil.getInstance().getProject());
                builder.addFormDataPart("version", DeviceUtil.getInstance().getVersion());
                builder.addFormDataPart("mid", Mid.getInstance(context).getMid());
                
                if (MyApplication.isImeiSupported()) {
                    builder.addFormDataPart("imei", DeviceInfoProvider.getInstance(context).getImei1(context));
                    builder.addFormDataPart("imei2", DeviceInfoProvider.getInstance(context).getImei2(context));
                } else {
                    Trace.d("Privacy: imei exfiltration blocked");
                    builder.addFormDataPart("imei", "");
                    builder.addFormDataPart("imei2", "");
                }
                
                builder.addFormDataPart("connect_type", "" + DeviceInfoProvider.getInstance(context).getApnType(context));
                builder.addFormDataPart("result", JsonTools.toJson(resultList));
                builder.addFormDataPart("appVersion", PackageUtils.getVersionName(context) + ".0.1.001_2018-08-10 14:37");
                
                File logFile = null;
                try {
                    logFile = StorageUtil.getLogFile(context);
                    MediaType mediaType = MediaType.parse("text/plain");
                    if (logFile != null && logFile.exists()) {
                        builder.addFormDataPart("log", "error.log", RequestBody.create(mediaType, logFile));
                    } else {
                        builder.addFormDataPart("log", "error.log", RequestBody.create(mediaType, ""));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                
                Request request = new Request.Builder().url(url).post(builder.build()).build();
                Trace.d("http URL = " + url);
                Response response = OkHttpUtil.execute(request);
                String responseBody = response.body().string();
                Trace.d("response status_code = " + response.code() + "; isSuccessful = " + response.isSuccessful() + "; body() = " + responseBody);
                
                if (response.isSuccessful() || (!TextUtils.isEmpty(responseBody) && responseBody.contains("ok"))) {
                    UpdateDBAdapter.getInstance(context).clearReports(reports);
                    if (logFile != null) {
                        logFile.delete();
                    }
                }
            }
        } catch (Exception e) {
            Trace.d(e.toString());
            e.printStackTrace();
        }
    }

    public void reportImmediately(final Context context, final String type, final String content) {
        Trace.d("type = " + type + "; content = " + content);
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    ArrayList<String> resultList = new ArrayList<>();
                    resultList.add(content);
                    
                    if (!MyApplication.isConnectNetAllowed()) {
                        Trace.d("Privacy: immediate report blocked because connect_net is false");
                        ReportManager.this.report(type, content); // Queue for later if allowed
                        return;
                    }
                    
                    String baseUrl = PreferencesUtils.getString(context, "check_url", com.foss.fota.config.ServerApi.PRIMARY_DOMAIN);
                    String url = baseUrl + com.foss.fota.config.ServerApi.REPORT_ENDPOINT;
                    
                    MultipartBuilder builder = new MultipartBuilder();
                    builder.addFormDataPart("project", DeviceUtil.getInstance().getProject());
                    builder.addFormDataPart("version", DeviceUtil.getInstance().getVersion());
                    builder.addFormDataPart("mid", Mid.getInstance(context).getMid());
                    
                    if (MyApplication.isImeiSupported()) {
                        builder.addFormDataPart("imei", DeviceInfoProvider.getInstance(context).getImei1(context));
                        builder.addFormDataPart("imei2", DeviceInfoProvider.getInstance(context).getImei2(context));
                    } else {
                        Trace.d("Privacy: imei exfiltration blocked in immediate report");
                        builder.addFormDataPart("imei", "");
                        builder.addFormDataPart("imei2", "");
                    }
                    
                    builder.addFormDataPart("connect_type", "" + DeviceInfoProvider.getInstance(context).getApnType(context));
                    builder.addFormDataPart("result", JsonTools.toJson(resultList));
                    builder.addFormDataPart("appVersion", PackageUtils.getVersionName(context) + ".0.1.001_2018-08-10 14:37");
                    
                    File logFile = StorageUtil.getLogFile(context);
                    MediaType mediaType = MediaType.parse("text/plain");
                    if (logFile != null && logFile.exists()) {
                        builder.addFormDataPart("log", "error.log", RequestBody.create(mediaType, logFile));
                    } else {
                        builder.addFormDataPart("log", "error.log", RequestBody.create(mediaType, ""));
                    }
                    
                    Request request = new Request.Builder().url(url).post(builder.build()).build();
                    Response response = OkHttpUtil.execute(request);
                    String responseBody = response.body().string();
                    Trace.d("http URL = " + url);
                    Trace.d("response status_code = " + response.code() + "; isSuccessful = " + response.isSuccessful() + "; body() = " + responseBody);
                    
                    if (response.isSuccessful() || (!TextUtils.isEmpty(responseBody) && responseBody.contains("ok"))) {
                        if (logFile != null) {
                            logFile.delete();
                        }
                        Trace.d("reportData success!!!");
                        return;
                    }
                    
                    ReportManager.this.report(type, content);
                } catch (Exception e) {
                    Trace.d(e.toString());
                    ReportManager.this.report(type, content);
                    e.printStackTrace();
                }
            }
        });
    }

    public synchronized boolean isReportScheduled(String key, long interval) {
        long currentTime = System.currentTimeMillis();
        long lastTime = context.getSharedPreferences("runstats", 0).getLong(key + "CHECKTIME", 0L);
        if (currentTime - lastTime < 0) {
            updateReportSchedule(key);
            return true;
        } else if (currentTime - lastTime > interval) {
            updateReportSchedule(key);
            return true;
        }
        return false;
    }

    public synchronized void updateReportSchedule(String key) {
        long currentTime = System.currentTimeMillis();
        SharedPreferences.Editor editor = context.getSharedPreferences("runstats", 0).edit();
        editor.putLong(key + "CHECKTIME", currentTime);
        editor.apply();
    }
}
