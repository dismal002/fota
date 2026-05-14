package com.foss.fota.update.report;

import android.content.Context;
import android.text.TextUtils;
import com.foss.fota.update.query.QueryInfo;
import com.foss.fota.update.Status;
import com.foss.fota.update.request.RequestResult;
import com.foss.fota.update.model.ReportModel;
import com.foss.fota.utils.Trace;
import com.foss.fota.utils.JsonTools;
import com.foss.fota.utils.PreferencesUtils;
import com.foss.fota.utils.StorageUtil;
import com.foss.fota.utils.DeviceUtil;
import com.foss.fota.utils.DeviceInfoProvider;
import org.apache.commons.compress.archivers.zip.UnixStat;
import java.io.File;

/* JADX INFO: compiled from: ReportData.java */
/* JADX INFO: loaded from: classes.dex */
public class ReportData {
    public static <T> String toJson(T t) {
        if (t != null) {
            return JsonTools.toJson(t);
        }
        return null;
    }

    public static void reportQuery(Context context, int checkType, int type, RequestResult result) {
        if (result != null) {
            try {
                ReportModel.RQuery.RQueryData data = new ReportModel.RQuery.RQueryData();
                data.status = result.isSuccess() ? 1 : 2;
                data.errCode = String.valueOf(result.getErrorCode());
                data.reason = result.getErrorMessage();
                data.time = System.currentTimeMillis() + "";
                data.version = QueryInfo.getInstance(context).getVersionModel() != null ? QueryInfo.getInstance(context).getVersionModel().getVersionName() : "";
                data.check_type = checkType;
                data.apn = DeviceInfoProvider.getInstance(context).getApnType(context);
                data.type = type;
                ReportManager.getInstance(context).report("check", toJson(new ReportModel.RQuery("check", data)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void reportQuery(Context context, boolean status, int type, String result) {
        // Compatibility alias
    }

    public static void reportQuery(Context context, String action) {
        // Compatibility alias
    }

    public static void reportStatus(Context context, String status, long duration) {
        reportDownload(context, status, duration);
    }

    public static void reportDownload(Context context, String status, long duration) {
        try {
            ReportModel.RDownload.RDownloadData data = new ReportModel.RDownload.RDownloadData();
            data.time = System.currentTimeMillis() + "";
            data.status = status;
            data.version = QueryInfo.getInstance(context).getVersionModel() != null ? QueryInfo.getInstance(context).getVersionModel().getVersionName() : "";
            data.duration = duration;
            data.background = com.foss.fota.update.download.DownVersion.getInstance(context).isDownloading() ? 1 : 0;
            data.type = com.foss.fota.update.query.QueryVersion.getInstance(context).getQueryType();
            data.apn = DeviceInfoProvider.getInstance(context).getApnType(context);
            ReportManager.getInstance(context).report("download", toJson(new ReportModel.RDownload("download", data)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void reportAction(Context context, boolean success, int status, String extra) {
        String statusStr = (success ? "success" : "fail") + "_status_" + status + (extra != null ? "_" + extra : "");
        reportAction(context, statusStr);
    }

    public static void reportAction(Context context, String status) {
        try {
            ReportModel.RInstall.RInstallData data = new ReportModel.RInstall.RInstallData();
            data.status = status;
            data.time = System.currentTimeMillis() + "";
            data.newVersion = QueryInfo.getInstance(context).getVersionModel() != null ? QueryInfo.getInstance(context).getVersionModel().getVersionName() : "";
            data.oldVersion = DeviceUtil.getInstance().getVersion();
            data.type = com.foss.fota.update.query.QueryVersion.getInstance(context).getQueryType();
            data.forced = ((Boolean) QueryInfo.getInstance(context).getPolicy("install_forced", Boolean.class)).booleanValue() ? 1 : 0;
            ReportManager.getInstance(context).report("upgrade", toJson(new ReportModel.RInstall("upgrade", data)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void reportInstallResult(Context context, boolean isSuccess, int errCode, String reason) {
        clearUpdateCache(context, isSuccess);
        try {
            ReportModel.RInstallResult.RInstallResultData data = new ReportModel.RInstallResult.RInstallResultData();
            data.time = System.currentTimeMillis() + "";
            data.oldVersion = formatVersion(PreferencesUtils.getString(context, "ota_original_version", ""));
            if (!TextUtils.isEmpty(reason) && reason.equalsIgnoreCase("ab")) {
                data.newVersion = QueryInfo.getInstance(context).getVersionModel() != null ? QueryInfo.getInstance(context).getVersionModel().getVersionName() : "";
            } else {
                data.newVersion = isSuccess ? DeviceUtil.getInstance().getVersion() : PreferencesUtils.getString(context, "ota_update_version", DeviceUtil.getInstance().getVersion());
            }
            data.type = PreferencesUtils.getInt(context, "ota_update_type", 1);
            data.status = isSuccess ? 1 : 0;
            data.errCode = mapErrorCode(errCode);
            if (reason == null) {
                reason = "";
            }
            data.reason = reason;
            ReportManager.getInstance(context).report("upgradeResult", toJson(new ReportModel.RInstallResult("upgradeResult", data)));
        } catch (Exception e) {
            Trace.d(e.getMessage());
        }
    }

    private static void clearUpdateCache(Context context, boolean isSuccess) {
        if (isSuccess) {
            try {
                String path = StorageUtil.getUpdatePackageFile(context);
                if (!TextUtils.isEmpty(path)) {
                    File file = new File(path);
                    if (file.exists()) {
                        file.delete();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static String formatVersion(String version) {
        String project = DeviceUtil.getInstance().getProject();
        Trace.d("local_version = " + version + "; project = " + project);
        if (!TextUtils.isEmpty(version) && version.contains("_other") && !TextUtils.isEmpty(project) && project.contains("_other")) {
            return version.substring(project.substring(0, project.lastIndexOf("_")).length() + 1, version.lastIndexOf("_"));
        }
        return version;
    }

    private static String mapErrorCode(int code) {
        switch (code) {
            case 401:
                return "4";
            case 402:
                return "6";
            case 403:
                return "8";
            case 404:
                return "9";
            case 408:
                return "7";
            case 409:
                return "3";
            case 411:
                return "5";
            case 412:
                return "B";
            case 413:
                return "1";
            case 414:
                return "2";
            case 415:
                return "C";
            case 416:
                return "10";
            case 417:
                return "11";
            case 418:
                return "12";
            case 419:
                return "13";
            case UnixStat.DEFAULT_FILE_PERM /* 420 */:
                return "14";
            default:
                return code + "";
        }
    }
}
