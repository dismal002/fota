package com.foss.fota.update.install;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import com.foss.fota.InstallResultActivity;
import com.foss.fota.utils.Trace;
import com.foss.fota.utils.PreferencesUtils;
import com.foss.fota.utils.DeviceUtil;

import com.foss.fota.config.Const;

public class InstallResult {
    public static boolean didEnterRecovery(Context context) {
        boolean didEnterRecovery;
        boolean isSuccess = false;
        synchronized (InstallResult.class) {
            didEnterRecovery = PreferencesUtils.d(context, "ota_enter_recovery");
            int downloadStatus = PreferencesUtils.b(context, "downlaodStatus", 0);
            int updateStatus = PreferencesUtils.b(context, "ota_update_status", 0);
            Trace.d("install verify,reboot_recovery:" + didEnterRecovery + ",old_reboot_flag:" + downloadStatus + ",status:" + updateStatus);
            if (didEnterRecovery || downloadStatus == 7 || updateStatus == 6) {
                if (!didEnterRecovery) {
                    PreferencesUtils.a(context, "ota_original_version", PreferencesUtils.b(context, "feedoldversion", ""));
                    PreferencesUtils.a(context, "ota_update_version", PreferencesUtils.b(context, "newVersion", ""));
                }
                try {
                    isSuccess = isUpdated(context, didEnterRecovery);
                    reportInstallResult(context, isSuccess);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                PreferencesUtils.a(context, "ota_enter_recovery", false);
                PreferencesUtils.a(context, "downlaodStatus", 0);
                com.foss.fota.update.query.QueryInfo.getInstance(context).getPolicyArray(context);
                if (1 == PreferencesUtils.b(context, "notifyFlag", 0)) {
                    com.foss.fota.update.Notice.updateShortcut(context, 1);
                }
                Trace.d("InstallResult", "FOTA_UPDATE_TYPE type = " + PreferencesUtils.b(context, "ota_update_type", -1));
                if (isSuccess) {
                    com.foss.fota.update.Status.setUpdateStatus(context, 0);
                    PreferencesUtils.a(context, "ota_original_version", com.foss.fota.utils.DeviceUtil.getInstance().getVersion());
                    if (shouldShowResultPop(context, didEnterRecovery)) {
                        startResultActivity(context);
                    }
                }
                updateFailCount(context, isSuccess);
                if (isSuccess) {
                    sendSuccessBroadcast(context);
                }
            }
        }
        return didEnterRecovery;
    }

    public static void updateFailCount(Context context, boolean isSuccess) {
        int count = PreferencesUtils.b(context, "ota_install_fail_count", 0);
        if (!isSuccess) {
            PreferencesUtils.a(context, "ota_install_fail_count", count + 1);
        }
    }

    public static void incrementFailCount(Context context, boolean isSegment) {
        int count = PreferencesUtils.b(context, "ota_install_fail_count", 0);
        Trace.d("InstallResult", "setVerifiedRecord, isSegment= " + isSegment);
        PreferencesUtils.a(context, "ota_install_fail_count", count + 1);
    }

    public static void b(Context context, boolean z) {
        incrementFailCount(context, z);
    }

    private static boolean shouldShowResultPop(Context context, boolean didEnterRecovery) {
        if (didEnterRecovery) {
            return PreferencesUtils.b(context, "ota_install_result_pop", false);
        }
        return PreferencesUtils.b(context, "noPopWinFlag", 1) == 0;
    }

    public static boolean isUpdated(Context context, boolean didEnterRecovery) {
        String currentVersion = com.foss.fota.utils.DeviceUtil.getInstance().getVersion();
        String originalVersion = PreferencesUtils.b(context, "ota_original_version", "");
        return (TextUtils.isEmpty(originalVersion) || originalVersion.equals(currentVersion)) ? false : true;
    }

    private static void startResultActivity(Context context) {
        Trace.d("forward InstallResultActivity");
        String currentVersion = com.foss.fota.utils.DeviceUtil.getInstance().getVersion();
        Intent intent = new Intent(context, InstallResultActivity.class);
        intent.addFlags(268435456);
        intent.putExtra("version", currentVersion);
        context.startActivity(intent);
    }

    private static void reportInstallResult(Context context, boolean isSuccess) {
        Trace.d("install report,install success:" + isSuccess);
        com.foss.fota.update.report.ReportData.reportAction(context, isSuccess, isSuccess ? 413 : 414, (String) null);
    }

    private static void sendSuccessBroadcast(Context context) {
        try {
            if (com.foss.fota.utils.DeviceUtil.getInstance().isFmSuccessEnabled()) {
                Trace.d("InstallResult", "sendUpdateSuccessBroadcast");
                Intent intent = new Intent();
                intent.setAction(Const.ACTION_OUT_UPDATE_SUCCESS);
                intent.addFlags(268435456);
                context.sendBroadcast(intent, Const.PERMISSION_FOTA);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
