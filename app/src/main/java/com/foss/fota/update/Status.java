package com.foss.fota.update;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.view.PointerIconCompat;
import android.text.TextUtils;
import com.foss.fota.BatteryService;
import com.foss.fota.JobServiceUtil.TaskIntentJobService;
import com.foss.fota.MyApplication;
import com.foss.fota.TaskIntentService;
import com.foss.fota.sysoper.Recovery;
import com.foss.fota.update.model.VersionModel;
import com.foss.fota.utils.Trace;
import com.foss.fota.utils.FileUtil;
import com.foss.fota.utils.IntentUtil;
import com.foss.fota.utils.NotifyManager;
import com.foss.fota.utils.PreferencesUtils;
import com.foss.fota.utils.StorageUtil;
import de.greenrobot.event.EventBus;
import java.io.File;

/* JADX INFO: compiled from: Status.java */
/* JADX INFO: loaded from: classes.dex */
public class Status {
    public static void setDownloadingStatus(Context context, VersionModel versionModel) {
        Trace.d(" ");
        PreferencesUtils.putInt(context, "ota_update_status", 1);
        PreferencesUtils.putString(context, "ota_original_version", com.foss.fota.utils.DeviceUtil.getInstance().getVersion());
        PreferencesUtils.putString(context, "ota_update_version", versionModel.getVersionName());
        PreferencesUtils.putInt(context, "ota_update_type", com.foss.fota.update.query.QueryVersion.getInstance(context).getQueryType());
        NotifyManager.getInstance(context).clearNotification(context, com.foss.fota.R.string.appbar_scrolling_view_behavior);
        setDownloadPath((String) com.foss.fota.update.query.QueryInfo.getInstance(context).getPolicy("download_path", String.class));
        Alarm.startCheckAlarm(context);
        if (Build.VERSION.SDK_INT >= 24) {
            IntentUtil.a(context, 3, TaskIntentJobService.class, 9, 1, "");
        } else {
            TaskIntentService.a(context, 9, 1, "");
        }
    }

    public static void setDownloadCompletedStatus(Context context) {
        Trace.d(" ");
        EventBus.getDefault().post(new EventMessage(200, PointerIconCompat.TYPE_CONTEXT_MENU, 0L, 0L, null));
        PreferencesUtils.putLong(context, "ota_install_delay_schedule", 0L);
        PreferencesUtils.putInt(context, "ota_update_status", 4);
        NotifyManager.getInstance(context).clearNotification(context, com.foss.fota.R.string.appbar_scrolling_view_behavior);
        Alarm.startInstallAlarm(context);
    }

    public static void performAutoInstall(Context context) {
        boolean isForced = ((Boolean) com.foss.fota.update.query.QueryInfo.getInstance(context).getPolicy("install_forced", Boolean.class)).booleanValue();
        Trace.d("download_completed_instal,force_install = " + isForced);
        if (com.foss.fota.update.install.Install.isAbUpdateSupported()) {
            if (!com.foss.fota.update.install.Install.isAbInstallSupported(context)) {
                Trace.d("no update reason : support ab update but not support reboot ab update");
                return;
            }
            int batteryThreshold = ((Integer) com.foss.fota.update.query.QueryInfo.getInstance(context).getPolicy("install_battery", Integer.class)).intValue();
            if (batteryThreshold <= 0) {
                batteryThreshold = 30;
            }
            if (com.foss.fota.update.install.Install.isBatteryEnough(context, batteryThreshold)) {
                com.foss.fota.update.report.ReportData.reportAction(context, "auto");
                com.foss.fota.update.install.Install.performInstall(context.getApplicationContext());
            } else {
                Trace.d("no update reason : battery not enough");
                context.startService(new Intent(context, (Class<?>) BatteryService.class));
                com.foss.fota.update.report.ReportData.reportAction(context, "cause_not_right_time");
                EventBus.getDefault().post(new EventMessage(300, 100, 0L, 417L, "ab"));
            }
            return;
        }
        if (isForced) {
            com.foss.fota.update.install.Install.performLegacyInstall(context);
        } else {
            Trace.d("no update reason : not support ab update and no force install");
            com.foss.fota.update.report.ReportData.reportAction(context, "cause_not_force_upgrade");
        }
    }

    public static void cancelUpdate(Context context) {
        Trace.d(" ");
        com.foss.fota.update.download.DownVersion.getInstance(context).cancelDownload();
        com.foss.fota.update.query.QueryInfo.getInstance(context).getPolicyArray(context);
        com.foss.fota.update.download.DownTask.stopAllTasks();
        com.foss.fota.update.report.ReportData.reportStatus(context, "cancel", 0L);
    }

    public static void clearUpdateData(Context context) {
        context.getSharedPreferences("fota_prefs", Context.MODE_PRIVATE).edit().clear().apply();
        cancelUpdate(context);
    }

    private static void setDownloadPath(String path) {
        if (!TextUtils.isEmpty(path) && path.contains("#")) {
            String[] parts = path.split("#");
            if (parts.length == 3) {
                StorageUtil.setDownloadPath(parts[0], parts[1], parts[2]);
                Trace.d("ota download path :path[0]=" + parts[0] + ",path[1]=" + parts[1] + ",path[2]=" + parts[2]);
            }
        }
    }

    public static void handlePostDownloadTask(Context context) {
        int status = PreferencesUtils.b(context, "ota_update_status", 0);
        Trace.d("downloadCompleteTask,status=" + status);
        if (status == 4) {
            performAutoInstall(context);
        } else if (status == 5) {
            if (PreferencesUtils.b(MyApplication.getInstance(), "ota_update_local", false)) {
                String localPath = PreferencesUtils.b(MyApplication.getInstance(), "ota_update_local_path", "");
                if (!TextUtils.isEmpty(localPath) && new File(localPath).exists()) {
                    Recovery.with(context).executeAb(localPath);
                } else {
                    cancelUpdate(MyApplication.getInstance());
                }
            } else {
                Recovery.with(context).executeAb(StorageUtil.getUpdatePackagePath(context));
            }
        } else if (status == 6) {
            com.foss.fota.update.install.Install.reportInstallResult(context);
        }
    }

    public static int getUpdateStatus(Context context) {
        return PreferencesUtils.b(context, "ota_update_status", 0);
    }

    public static void setUpdateStatus(Context context, int status) {
        PreferencesUtils.putInt(context, "ota_update_status", status);
    }

    public static void b(Context context, int status) {
        setUpdateStatus(context, status);
    }

    public static void a(Context context, int status) {
        setUpdateStatus(context, status);
    }

    public static int progressLayout(Context context) {
        return getUpdateStatus(context);
    }

    public static int f(Context context) {
        return getUpdateStatus(context);
    }

    public static void c(Context context) {
        Trace.d("Status.c called");
    }
}
