package com.foss.fota.update.query;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.view.PointerIconCompat;
import android.text.TextUtils;
import com.foss.fota.MyJobService;
import com.foss.fota.TaskIntentService;
import com.foss.fota.update.model.VersionModel;
import com.foss.fota.update.Status;
import com.foss.fota.update.Notice;
import com.foss.fota.update.EventMessage;
import com.foss.fota.update.report.ReportData;
import com.foss.fota.update.request.RequestResult;
import com.foss.fota.utils.Trace;
import com.foss.fota.utils.FileUtil;
import com.foss.fota.utils.JsonTools;
import com.foss.fota.utils.DeviceUtil;
import com.foss.fota.utils.PreferencesUtils;
import com.foss.fota.utils.ActivityStackUtil;
import de.greenrobot.event.EventBus;
import org.json.JSONObject;

public class ParserVersion {
    public void parse(Context context, RequestResult result) {
        if (result != null) {
            if (result.isSuccess()) {
                if (Status.getUpdateStatus(context) >= 4) {
                    Notice.showUpdateNotice(context);
                    Trace.d("query succeed, but a system has already been installed successfully");
                    return;
                } else {
                    parseJson(context, result.getData());
                    return;
                }
            }
            try {
                Status.handlePostDownloadTask(context);
                Trace.d("query version error, mid reset");
                postErrorEvent(result);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void postErrorEvent(RequestResult result) {
        Trace.d("Status_code = " + result.getHttpCode());
        switch (result.getErrorCode()) {
            case 3008:
                EventBus.getDefault().post(new EventMessage(100, 3008, result.getHttpCode(), 0L, result.getErrorMessage()));
                break;
            default:
                EventBus.getDefault().post(new EventMessage(100, 3010, result.getHttpCode(), 0L, result.getErrorMessage()));
                break;
        }
    }

    public void parseJson(Context context, String json) {
        try {
            Trace.d("content = " + json);
            JSONObject jSONObject = new JSONObject(json);
            if (jSONObject.has(NotificationCompat.CATEGORY_STATUS)) {
                Trace.d("status = " + jSONObject.getInt(NotificationCompat.CATEGORY_STATUS));
            }
            if (jSONObject.has("flag")) {
                JSONObject flagObject = new JSONObject(jSONObject.getString("flag"));
                if (flagObject.has("mid")) {
                    Trace.d("mid = " + flagObject.getString("mid"));
                    PreferencesUtils.putString(context, "mid", flagObject.getString("mid"));
                }
                if (flagObject.has("check_freq")) {
                    Trace.d("query freq = " + flagObject.getLong("check_freq"));
                    PreferencesUtils.putLong(context, "check_freq", flagObject.getLong("check_freq"));
                }
                if (Build.VERSION.SDK_INT >= 21 && flagObject.has("job_schedule_time")) {
                    Trace.d("job schedule time = " + flagObject.getString("job_schedule_time"));
                    PreferencesUtils.putString(context, "job_schedule_time", flagObject.getString("job_schedule_time"));
                    MyJobService.schedule(context);
                }
                if (flagObject.has("job_schedule_downloading_time")) {
                    Trace.d("job schedule downloading time = " + flagObject.getString("job_schedule_downloading_time"));
                    PreferencesUtils.putString(context, "job_schedule_downloading_time", flagObject.getString("job_schedule_downloading_time"));
                }
                if (flagObject.has("isFull")) {
                    Trace.d("query is full = " + flagObject.getInt("isFull"));
                    PreferencesUtils.putInt(context, "isFull", flagObject.getInt("isFull"));
                }
                if (flagObject.has("sendId")) {
                    Trace.d("query is sender id = " + flagObject.getString("sendId"));
                    PreferencesUtils.putString(context, "sever_send_id", flagObject.getString("sendId"));
                }
                if (flagObject.has("isupgrade")) {
                    Trace.d("upgrade = " + flagObject.getInt("isupgrade"));
                    PreferencesUtils.putInt(context, "isupgrade", flagObject.getInt("isupgrade"));
                }
            }
            if (jSONObject.has("version")) {
                Trace.d("json version exist");
                handleVersion(context, jSONObject.getString("version"));
                return;
            }
            postEvent(context, PointerIconCompat.TYPE_HELP, null);
        } catch (Throwable e) {
            e.printStackTrace();
            postEvent(context, 3008, null);
        }
    }

    private void handleVersion(Context context, String versionJson) throws Throwable {
        if (!TextUtils.isEmpty(versionJson)) {
            Trace.d(versionJson);
            VersionModel versionModel = (VersionModel) JsonTools.fromJson(versionJson, VersionModel.class);
            if (versionModel != null) {
                String currentVersion = DeviceUtil.getInstance().getVersion();
                if (!TextUtils.isEmpty(versionModel.getVersionName()) && versionModel.getVersionName().equals(currentVersion)) {
                    EventBus.getDefault().post(new EventMessage(100, PointerIconCompat.TYPE_HAND, 0L, 0L, null));
                    ReportData.reportAction(context, "cause_same_version");
                    return;
                }
                Trace.d("new_version.getSourcename() = " + versionModel.getSourcename() + "; device_version = " + currentVersion);
                if (TextUtils.isEmpty(versionModel.getSourcename()) || versionModel.getSourcename().equals(currentVersion)) {
                    VersionModel oldVersionModel = QueryInfo.getInstance(context).getVersionModel();
                    QueryInfo.getInstance(context).saveVersionJson(context, versionJson);
                    int currentStatus = Status.getUpdateStatus(context);
                    Trace.d("version_status = " + currentStatus);
                    boolean isPolicyChanged = isPolicyChanged(context, versionJson);
                    
                    if (oldVersionModel != null) {
                        if (versionModel.getVersionName() != null && !versionModel.getVersionName().equals(oldVersionModel.getVersionName())) {
                            resetInstallFailInfo(context, versionModel.getVersionName());
                            Trace.d("new version version name diff");
                            Status.setDownloadingStatus(context, versionModel);
                            postEvent(context, PointerIconCompat.TYPE_WAIT, versionModel);
                            return;
                        }
                        if (versionModel.getDeltaurl() != null && !versionModel.getDeltaurl().equals(oldVersionModel.getDeltaurl())) {
                            Trace.d("new version delta url diff");
                            resetInstallFailInfo(context, versionModel.getVersionName());
                            Status.setDownloadingStatus(context, versionModel);
                            postEvent(context, PointerIconCompat.TYPE_WAIT, versionModel);
                            return;
                        }
                        if (isPolicyChanged) {
                            Trace.d("new version delta content diff");
                            Status.setDownloadingStatus(context, versionModel);
                            postEvent(context, 1005, versionModel);
                            return;
                        }
                        processVersionStatus(context, versionModel, currentStatus, isPolicyChanged);
                        return;
                    }
                    processVersionStatus(context, versionModel, currentStatus, isPolicyChanged);
                    return;
                }
                return;
            }
            Trace.d("new version json parser error");
            ReportData.reportAction(context, "cause_parser_error");
            postEvent(context, PointerIconCompat.TYPE_HELP, null);
            return;
        }
        Trace.d("new version json content null");
        ReportData.reportAction(context, "cause_parser_error");
        postEvent(context, PointerIconCompat.TYPE_HELP, null);
    }

    public void processVersionStatus(Context context, VersionModel versionModel, int status, boolean isChanged) {
        Trace.d("version_status = " + status + "; isChange = " + isChanged);
        if (status == 0 || isChanged) {
            resetInstallFailInfo(context, versionModel.getVersionName());
            Status.setDownloadingStatus(context, versionModel);
            postEvent(context, PointerIconCompat.TYPE_CONTEXT_MENU, versionModel);
            return;
        }
        postEvent(context, PointerIconCompat.TYPE_HAND, versionModel);
    }

    private void postEvent(Context context, int eventType, VersionModel versionModel) {
        switch (eventType) {
            case PointerIconCompat.TYPE_CONTEXT_MENU /* 1001 */:
            case PointerIconCompat.TYPE_WAIT /* 1004 */:
                if (!checkRootAndReport(context, versionModel)) {
                    if (!startDownload(context, versionModel)) {
                        EventBus.getDefault().post(new EventMessage(100, PointerIconCompat.TYPE_HELP, 0L, 0L, null));
                        return;
                    }
                    RequestResult result = new RequestResult();
                    result.setSuccess(true);
                    ReportData.reportQuery(context, 4, 1, result);
                    handlePostQuery(context);
                } else {
                    return;
                }
                break;
            case PointerIconCompat.TYPE_HAND /* 1002 */:
                if (!ActivityStackUtil.isAppInForeground()) {
                    Notice.showUpdateNotice(context);
                }
                performBackgroundAction(context);
                break;
            case PointerIconCompat.TYPE_HELP /* 1003 */:
                if (QueryVersion.getInstance(context).isDamagedOrUrlChanged(versionModel)) {
                    EventBus.getDefault().post(new EventMessage(100, 404, 0L, 0L, null));
                    return;
                }
                break;
            case 1005:
                if (!checkRootAndReport(context, versionModel)) {
                    performBackgroundAction(context);
                } else {
                    return;
                }
                break;
            case 3008:
                ReportData.reportAction(context, "cause_parser_error");
                break;
        }
        EventBus.getDefault().post(new EventMessage(100, eventType, 0L, 0L, null));
    }

    private boolean startDownload(Context context, VersionModel versionModel) {
        com.foss.fota.update.download.DownVersion.getInstance(context).cancelDownload();
        com.foss.fota.update.download.DownVersion.getInstance(context).startDownload(versionModel);
        return true;
    }

    public void resetInstallFailInfo(Context context, String version) {
        try {
            PreferencesUtils.putInt(context, "ota_install_fail_count", 0);
            PreferencesUtils.putString(context, "ota_install_fail_version", version);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isPolicyChanged(Context context, String json) {
        String newPolicy = "";
        try {
            String oldPolicy = PreferencesUtils.getString(context, "policy_content", "");
            JSONObject jSONObject = new JSONObject(json);
            if (jSONObject.has("policy")) {
                newPolicy = jSONObject.getString("policy");
            }
            Trace.d("policyJson = " + newPolicy + "; old_policy = " + oldPolicy);
            if (TextUtils.isEmpty(newPolicy) || oldPolicy.equalsIgnoreCase(newPolicy)) {
                return false;
            }
            PreferencesUtils.putString(context, "policy_content", newPolicy);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void handlePostQuery(Context context) {
        try {
            if (DeviceUtil.getInstance().isGoogleOta()) {
                Trace.d("send broadcast for google ota");
                Intent intent = new Intent();
                intent.setAction("com.foss.fota.google.ota");
                intent.addFlags(268435456);
                context.sendBroadcast(intent, "com.foss.fota.permission.GOOGLE_OTA");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void performBackgroundAction(Context context) {
        int status = Status.getUpdateStatus(context);
        Trace.d("version_status = " + status);
        if (status == 2) {
            com.foss.fota.update.download.DownVersion.getInstance(context).stopDownload();
            try {
                Thread.sleep(2000L);
            } catch (Exception e) {
                e.printStackTrace();
            }
            com.foss.fota.update.download.DownVersion.getInstance(context).startDownload(1);
            return;
        }
        if (status == 4) {
            boolean isForced = ((Boolean) QueryInfo.getInstance(context).getPolicy("install_forced", Boolean.class)).booleanValue();
            if (isForced) {
                Trace.d("force_install = " + isForced);
                com.foss.fota.update.install.Install.performAutoInstall(context);
                return;
            }
            return;
        }
        TaskIntentService.start(context, 9, 1, "");
    }

    private boolean checkRootAndReport(Context context, VersionModel versionModel) {
        if (!QueryVersion.getInstance(context).isDamagedOrUrlChanged(versionModel)) {
            return false;
        }
        if (Status.getUpdateStatus(context) == 2) {
            com.foss.fota.update.download.DownVersion.getInstance(context).cancelDownload();
        }
        EventBus.getDefault().post(new EventMessage(100, 404, 0L, 0L, null));
        ReportData.reportAction(context, "cause_device_rooted");
        return true;
    }
}
