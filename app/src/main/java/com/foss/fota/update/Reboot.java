package com.foss.fota.update;

import android.content.Context;
import android.os.Build;
import com.foss.fota.MyJobService;
import com.foss.fota.utils.Trace;
import com.foss.fota.utils.NotifyManager;
import com.foss.fota.utils.PreferencesUtils;

/* JADX INFO: compiled from: Reboot.java */
/* JADX INFO: loaded from: classes.dex */
public class Reboot {
    public void performStartupCheck(Context context) {
        try {
            try {
                initDebugLog(context);
                int status = PreferencesUtils.b(context, "ota_update_status", 0);
                if (status == 5 && com.foss.fota.update.install.InstallResult.isUpdated(context, false)) {
                    PreferencesUtils.putInt(context, "ota_update_status", 6);
                    status = PreferencesUtils.b(context, "ota_update_status", 0);
                }
                showPostUpdateNotification(context, com.foss.fota.update.install.InstallResult.didEnterRecovery(context), status);
                Trace.d("startup_verify status = " + status);
                com.foss.fota.update.Notice.cancelUpdate(context);
                if (status == 2) {
                    PreferencesUtils.putInt(context, "ota_update_status", 3);
                } else if (status >= 4) {
                    Status.handlePostDownloadTask(context);
                }
                Alarm.cancelUpdate(context);
            } finally {
                try {
                    scheduleNextJob(context);
                } catch (Exception e) {
                }
            }
        } catch (Exception e2) {
            e2.printStackTrace();
            try {
                scheduleNextJob(context);
            } catch (Exception e3) {
            }
        }
    }

    private void initDebugLog(Context context) {
        try {
            Trace.setDebugEnabled(PreferencesUtils.getBoolean(context, "debug_status", false));
            Trace.setLogPath(PreferencesUtils.b(context, "debug_log_path", ""));
        } catch (Exception e) {
        }
    }

    private void showPostUpdateNotification(Context context, boolean isRebootRecovery, int status) {
        try {
            Trace.d("Reboot", "isNotifyMessage,isReboot_recovery = " + isRebootRecovery + " status = " + status);
            int noticeType = ((Integer) com.foss.fota.update.query.QueryInfo.getInstance(context).getPolicy("query_notice_type", Integer.class)).intValue();
            boolean isResident = ((Boolean) com.foss.fota.update.query.QueryInfo.getInstance(context).getPolicy("query_notice_resident", Boolean.class)).booleanValue();
            int installNoticeType = ((Integer) com.foss.fota.update.query.QueryInfo.getInstance(context).getPolicy("install_notice_type", Integer.class)).intValue();
            boolean isInstallResident = ((Boolean) com.foss.fota.update.query.QueryInfo.getInstance(context).getPolicy("install_notice_resident", Boolean.class)).booleanValue();
            Trace.d("Reboot", "ota notice : notice_type = " + noticeType + "||notice_resident = " + isResident + "||install_notice_type = " + installNoticeType + "||install_notice_resident = " + isInstallResident);
            if ((!isRebootRecovery && noticeType == 0 && isResident) || (installNoticeType == 0 && isInstallResident && status != 0)) {
                if (status == 4) {
                    NotifyManager.getInstance(context).showDownloadComplete(context, true);
                } else {
                    NotifyManager.getInstance(context).showNewVersion(context, true);
                }
            }
        } catch (Exception e) {
            Trace.d("Reboot", "Exception : " + e.toString());
        }
    }

    private void scheduleNextJob(Context context) {
        if (Build.VERSION.SDK_INT >= 21) {
            MyJobService.schedule(context, 1000, 300000L, 1440 * MyJobService.INTERVAL_UNIT);
        }
    }
}
