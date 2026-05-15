package com.foss.fota.update.query;

import android.content.Context;
import android.os.Process;
import android.os.SystemClock;
import androidx.core.view.PointerIconCompat;
import android.text.TextUtils;
import com.foss.fota.update.model.VersionModel;
import com.foss.fota.update.Status;
import com.foss.fota.update.Alarm;
import com.foss.fota.utils.Trace;
import com.foss.fota.utils.Encrypt;
import com.foss.fota.utils.Mid;
import com.foss.fota.utils.NetWorkUtil;
import com.foss.fota.utils.OkHttpUtil;
import com.foss.fota.utils.PackageUtils;
import com.foss.fota.utils.PreferencesUtils;
import com.foss.fota.utils.SecurityUtil;
import de.greenrobot.event.EventBus;
import java.util.HashMap;

/* JADX INFO: compiled from: QueryVersion.java */
/* JADX INFO: loaded from: classes.dex */
public class QueryVersion {
    private static QueryVersion instance = null;
    private static int failCount = 0;
    private boolean isQuerying;
    private Context context;
    private int queryType;
    private int queryMode = 1;

    private QueryVersion(Context context) {
        this.context = context.getApplicationContext();
    }

    public static QueryVersion getInstance(Context context) {
        if (instance == null) {
            synchronized (QueryVersion.class) {
                if (instance == null) {
                    instance = new QueryVersion(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    public void checkSchedule() {
        Alarm.startCheckAlarm(this.context);
        boolean isConnected = NetWorkUtil.a(this.context);
        boolean isOverSchedule = isOverSchedule();
        boolean isOverActivateTime = com.foss.fota.update.query.QueryActivate.a(this.context);
        if (PackageUtils.a(Process.myUid()) != 0) {
            Trace.d("onQuerySchedule,not system user,return");
            return;
        }
        Trace.d("isOverSchedule = " + isOverSchedule + "; isConnected = " + isConnected + "; isOverActivateTime = " + isOverActivateTime);
        if (isOverActivateTime && isConnected && isOverSchedule) {
            onQueryScheduleTask();
            checkClearCache(this.context);
        }
    }

    public int getQueryMode() {
        return this.queryMode;
    }

    public int getQueryType() {
        return this.queryType;
    }

    public void setQueryMode(int mode) {
        this.queryMode = mode;
    }

    public void startQuery(int type, int mode) {
        Trace.d("query_type = " + type + "; isQuerying = " + this.isQuerying);
        synchronized (this) {
            if (this.isQuerying) {
                EventBus.getDefault().post(new com.foss.fota.update.EventMessage(100, 1000, 0L, 0L, null));
                return;
            }
            this.isQuerying = true;
            this.queryType = type;
            this.queryMode = mode;
            runQueryThread();
        }
    }

    public void startNormalQuery(int type) {
        startQuery(type, 1);
    }

    public void b(int type) {
        startNormalQuery(type);
    }

    private void runQueryThread() {
        new Thread(new Runnable() { // from class: com.foss.fota.update.query.QueryVersion.1
            @Override // java.lang.Runnable
            public void run() {
                Process.setThreadPriority(10);
                Trace.d("thread start");
                performQueryTask();
                Alarm.startCheckAlarm(QueryVersion.this.context);
                Trace.d("thread end");
            }
        }).start();
    }

    public void onQueryScheduleTask() {
        Trace.d("onQueryScheduleTask");
        this.queryMode = 1;
        startQuery(1, this.queryMode);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void performQueryTask() {
        try {
            try {
                Trace.d("onQueryTask:start");
                if (!com.foss.fota.MyApplication.isConnectNetAllowed()) {
                    Trace.d("Privacy: query blocked because connect_net is false");
                    this.isQuerying = false;
                    EventBus.getDefault().post(new com.foss.fota.update.EventMessage(100, 3010, 0L, 0L, null));
                    return;
                }
                EventBus.getDefault().post(new com.foss.fota.update.EventMessage(100, PointerIconCompat.TYPE_VERTICAL_TEXT, 0L, 0L, null));
                Status.clearUpdateData(this.context);
                if (PreferencesUtils.b(this.context, "ota_update_status", 0) == 0) {
                    com.foss.fota.update.query.QueryInfo.getInstance(this.context).getPolicyArray(this.context);
                }
                checkRomDamaged();
                if (this.queryType == 2) {
                    SystemClock.sleep(500L);
                }
                String url = "";
                String baseUrl = PreferencesUtils.b(this.context, "check_url", com.foss.fota.config.ServerApi.PRIMARY_DOMAIN);
                if (this.queryMode == 1) {
                    url = baseUrl + com.foss.fota.config.ServerApi.QUERY_ENDPOINT;
                } else if (this.queryMode == 2) {
                    url = baseUrl + com.foss.fota.config.ServerApi.FULL_QUERY_ENDPOINT;
                }
                Trace.d("query  url = " + url);
                StringBuffer params = com.foss.fota.update.request.RequestParam.getRequestParams(this.context);
                HashMap<String, String> map = new HashMap<>();
                Trace.d("query  post params = " + params.toString());
                String encryptedParams = Encrypt.getType().a(params.toString());
                map.put("key", encryptedParams);
                map.put("shaKey", SecurityUtil.a(encryptedParams));
                com.foss.fota.update.request.RequestResult result = new com.foss.fota.update.request.RequestBase(url).execute(map);
                Trace.d("query result : http status code = " + result.initData() + " error_code = " + result.getType() + " error_message = " + result.getStatus());
                com.foss.fota.update.report.ReportData.reportQuery(this.context, this.queryType, this.queryMode, result);
                new com.foss.fota.update.query.ParserVersion().parse(this.context, result);
                handleQueryResult(result, baseUrl);
                this.isQuerying = false;
            } catch (Exception e) {
                Trace.d(e.toString());
                e.printStackTrace();
                try {
                    if (this.context.getFilesDir() == null) {
                        com.foss.fota.update.report.ReportData.reportAction(this.context, "cause_not_enough");
                        EventBus.getDefault().post(new com.foss.fota.update.EventMessage(100, 3005, 0L, 0L, null));
                    } else {
                        EventBus.getDefault().post(new com.foss.fota.update.EventMessage(100, 3010, 0L, 0L, null));
                    }
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
                this.isQuerying = false;
            }
        } catch (Throwable th) {
            this.isQuerying = false;
            throw th;
        }
    }

    public void handleQueryResult(com.foss.fota.update.request.RequestResult result, String baseUrl) {
        if (result != null) {
            if (result.c()) {
                PreferencesUtils.putLong(this.context, "check_last_time", System.currentTimeMillis());
                return;
            }
            failCount++;
            if (failCount >= 3) {
                failCount = 0;
                PreferencesUtils.putLong(this.context, "check_last_time", System.currentTimeMillis());
            }
            OkHttpUtil.clearDnsCache();
            switchCheckUrl(baseUrl);
        }
    }

    private boolean isOverSchedule() {
        return PreferencesUtils.b(this.context, "check_last_time", 0L) + Alarm.getCheckInterval(this.context) <= System.currentTimeMillis();
    }

    private boolean handleFailCounts() {
        int counts = PreferencesUtils.b(this.context, "check_fail_counts");
        if (counts >= 3) {
            PreferencesUtils.putInt(this.context, "check_fail_counts", 0);
            return true;
        }
        PreferencesUtils.a(this.context, "check_fail_counts", counts + 1);
        return false;
    }

    private void switchCheckUrl(String currentUrl) {
        try {
            if (handleFailCounts()) {
                if (!TextUtils.isEmpty(currentUrl) && com.foss.fota.config.ServerApi.PRIMARY_DOMAIN.equals(currentUrl)) {
                    PreferencesUtils.putString(this.context, "check_url", com.foss.fota.config.ServerApi.SECONDARY_DOMAIN);
                } else {
                    PreferencesUtils.putString(this.context, "check_url", com.foss.fota.config.ServerApi.PRIMARY_DOMAIN);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkRomDamaged() {
        if (PreferencesUtils.b(this.context, "rom_damaged", false) && !PreferencesUtils.b(this.context, "rom_damaged_version", "FOTA").equals(com.foss.fota.utils.DeviceUtil.getInstance().getVersion())) {
            PreferencesUtils.putBoolean(this.context, "rom_damaged", false);
            PreferencesUtils.b(this.context, "rom_damaged_version", "FOTA");
        }
    }

    public boolean isDamagedOrUrlChanged(VersionModel versionModel) {
        if (this.queryMode != 1) {
            return false;
        }
        boolean isDamaged = PreferencesUtils.b(this.context, "rom_damaged", false);
        boolean isUrlChanged = isDeltaUrlChanged(this.context, versionModel);
        boolean isUpgrade = PreferencesUtils.b(this.context, "isupgrade", 0) == 1;
        Trace.d("isDamaged = " + isDamaged + "; urlChanged = " + isUrlChanged);
        if (isUrlChanged) {
            PreferencesUtils.putBoolean(this.context, "rom_damaged", false);
            return false;
        }
        return isUpgrade && isDamaged;
    }

    public boolean isFullUpdate() {
        boolean isFull = PreferencesUtils.b(this.context, "ota_update_type", 1) == 2;
        Trace.d("isFullUpdate = " + isFull);
        return isFull;
    }

    public boolean isDeltaUrlChanged(Context context, VersionModel versionModel) {
        if (versionModel == null) {
            Trace.d("version == null");
            return false;
        }
        try {
            String savedUrl = PreferencesUtils.b(context, "deltaurl", "");
            String newUrl = versionModel.getDeltaurl();
            if (TextUtils.isEmpty(newUrl) || savedUrl.equals(newUrl)) {
                return false;
            }
            Trace.d("isChangeDeltaUrl = true");
            PreferencesUtils.putString(context, "deltaurl", newUrl);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void checkClearCache(final Context context) {
        new Thread(new Runnable() { // from class: com.foss.fota.update.query.QueryVersion.2
            @Override // java.lang.Runnable
            public void run() {
                try {
                    Thread.sleep(60000L);
                    int clearCache = ((Integer) com.foss.fota.update.query.QueryInfo.getInstance(context).getPolicy("clear_cache", Integer.class)).intValue();
                    VersionModel version = com.foss.fota.update.query.QueryInfo.getInstance(context).getVersionModel();
                    if (version == null) return;
                    String deltaurl = version.getDeltaurl();
                    String cachedUrl = PreferencesUtils.b(context, "cache_url", "");
                    if (clearCache == 1 && !cachedUrl.equals(deltaurl)) {
                        Trace.d("execute clear cache ");
                        Status.clearUpdateData(context);
                        PreferencesUtils.putString(context, "cache_url", deltaurl);
                        EventBus.getDefault().post(new com.foss.fota.update.EventMessage(100, PointerIconCompat.TYPE_CELL, 0L, 0L, null));
                        com.foss.fota.update.report.ReportData.reportAction(context, "cause_clean_cache");
                        Thread.sleep(5000L);
                        QueryVersion.getInstance(context).onQueryScheduleTask();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
