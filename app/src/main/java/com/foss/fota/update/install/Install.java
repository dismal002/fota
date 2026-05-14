package com.foss.fota.update.install;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Process;
import androidx.core.app.NotificationCompat;
import com.foss.fota.FotaPopInstallWindow;
import com.foss.fota.MyApplication;
import com.foss.fota.sysoper.Recovery;
import com.foss.fota.update.model.VersionModel;
import com.foss.fota.utils.Trace;
import com.foss.fota.utils.FileUtil;
import com.foss.fota.utils.DeviceUtil;
import com.foss.fota.utils.PreferencesUtils;
import com.foss.fota.utils.StorageUtil;
import com.foss.fota.update.query.QueryInfo;
import com.foss.fota.update.report.ReportData;
import de.greenrobot.event.EventBus;
import java.io.File;
import java.util.Calendar;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class Install {
    private static boolean isInstalling = false;

    public static boolean doInstall(final Context context, final String packagePath) {
        if (!isAbUpdate()) {
            PreferencesUtils.putInt(context, "ota_update_status", 0);
            PreferencesUtils.putBoolean(context, "ota_enter_recovery", true);
        }
        boolean isOldReboot = DeviceUtil.getInstance().isOldReboot();
        Trace.d("update file path = " + packagePath + "; isOldReboot " + isOldReboot);
        StorageUtil.init(context);
        /* Removing insecure legacy broadcast mechanism
        if (isOldReboot) {
            Intent intent = new Intent("android.intent.action.AdupsFota.WriteCommandReceiver");
            intent.putExtra("PackageFileName", packagePath);
            context.sendBroadcast(intent);
            return true;
        }
        */
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (isAbUpdate()) {
                        if (!isInstallAllowed(context)) {
                            Trace.d("no install reason : support ab update but not support reboot ab install");
                        } else {
                            Recovery.with(context).executeAb(packagePath);
                        }
                    } else if (Recovery.with(context).execute(packagePath) <= 0) {
                        Trace.d("install execute error!");
                        ReportData.reportAction(context, false, 415, (String) null);
                    }
                } catch (Exception e) {
                    Trace.d("install exception : " + e.getMessage());
                }
            }
        }).start();
        return true;
    }

    public static boolean isInstallAllowed(Context context) {
        if (DeviceUtil.getInstance().getBuildVersion() >= 517) {
            return true;
        }
        Trace.d("version is low");
        return false;
    }

    public static boolean isAbUpdate() {
        try {
            return ((Boolean) Class.forName("android.os.SystemProperties")
                    .getMethod("getBoolean", String.class, Boolean.TYPE).invoke(null, "ro.build.ab_update", false))
                    .booleanValue();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void verifyAndInstall(Context context) {
        InstallParserBase parser;
        int status;
        if (isAbUpdate()) {
            EventBus.getDefault().post(new com.foss.fota.update.EventMessage(300, 100, 0L, 5L, "ab"));
        }
        VersionModel versionModel = QueryInfo.getInstance(context).getVersionModel();
        String sha = versionModel != null ? versionModel.getSha() : null;
        if (hasMd5Sum(StorageUtil.getUpdatePackagePath(context))) {
            parser = new InstallParser1();
        } else {
            parser = new InstallParser2();
        }
        try {
            status = parser.a(context, StorageUtil.getUpdatePackagePath(context), sha);
        } catch (Throwable e) {
            ReportData.reportAction(context, "cause_verify_exception");
            status = 0;
        }
        Trace.d("verify package status = " + status);
        if (status == 405) {
            String path = StorageUtil.getUpdatePackagePath(context);
            File file1 = new File("/data/media/0/fossfota/update.zip");
            File file2 = new File("/data/media/fossfota/update.zip");
            File file3 = new File("/storage/emulated/0/fossfota/update.zip");
            StorageUtil.init(StorageUtil.getBestStorageRoot(context, false));
            if (Build.VERSION.SDK_INT >= 21) {
                if (file1.exists()) {
                    if (FileUtil.a("/data/media/0/fossfota/update.zip", com.foss.fota.config.Const.UPDATE_ZIP_PATH,
                            true)) {
                        path = com.foss.fota.config.Const.UPDATE_ZIP_PATH;
                    }
                } else if (file2.exists()) {
                    if (FileUtil.a("/data/media/fossfota/update.zip", com.foss.fota.config.Const.UPDATE_ZIP_PATH,
                            true)) {
                        path = com.foss.fota.config.Const.UPDATE_ZIP_PATH;
                    }
                } else if (file3.exists()) {
                    if (FileUtil.a("/storage/emulated/0/fossfota/update.zip",
                            com.foss.fota.config.Const.UPDATE_ZIP_PATH, true)) {
                        path = com.foss.fota.config.Const.UPDATE_ZIP_PATH;
                    }
                }
            }
            if (versionModel != null) {
                PreferencesUtils.putString(context, "ota_update_version", versionModel.getVersionName());
                PreferencesUtils.putBoolean(context, "ota_install_result_pop",
                        (Boolean) QueryInfo.getInstance(context).getPolicy("install_result_pop", Boolean.class));
                PreferencesUtils.putInt(context, "notifyFlag",
                        (Integer) QueryInfo.getInstance(context).getPolicy("query_notice_type", Integer.class));
            }
            PreferencesUtils.putBoolean(context, "ota_update_local", false);
            doInstall(context, path);
        } else {
            Trace.d("no install reason : status not correct");
            com.foss.fota.update.Status.b(context, 1);
            QueryInfo.getInstance(context).init(context);
            ReportData.reportAction(context, false, status, parser.a());
        }
        EventBus.getDefault().post(new com.foss.fota.update.EventMessage(300, status, 0L, 0L, null));
    }

    public static void startInstall(final Context context) {
        if (Process.myUid() != 1000 && Process.myUid() != 0) { // System or root
            Trace.d("no update reason : not system user");
            return;
        }
        if (isInstalling) {
            ReportData.reportAction(context, "cause_installing");
            return;
        }
        if (isAbUpdate()) {
            EventBus.getDefault().post(new com.foss.fota.update.EventMessage(300, 100, 0L, 5L, "ab"));
        }
        isInstalling = true;
        Trace.d("ota install update start");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (QueryInfo.getInstance(context).getVersionModel() != null) {
                        Trace.d("ota install normal ");
                        verifyAndInstall(context);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Trace.d("update exception : " + e.getMessage());
                    ReportData.reportAction(context, "cause_exception");
                }
                isInstalling = false;
            }
        }).start();
    }

    public static boolean hasMd5Sum(String path) {
        ZipFile zipFile = null;
        try {
            zipFile = new ZipFile(path);
            ZipEntry entry = zipFile.getEntry("md5sum");
            if (entry != null) {
                try {
                    zipFile.close();
                } catch (Exception e) {
                }
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (zipFile != null) {
                try {
                    zipFile.close();
                } catch (Exception e) {
                }
            }
        }
        return false;
    }

    public static void forceUpdate(Context context) {
        String[] timeRange = (String[]) QueryInfo.getInstance(context).getPolicy("install_time", String[].class);
        if (PreferencesUtils.getInt(context, "ota_update_status", 0) != 4) {
            Trace.d("no force update reason : status not correct");
            ReportData.reportAction(context, "cause_not_dlcomplete");
            return;
        }
        int startHour, endHour;
        try {
            startHour = Integer.parseInt(timeRange[0]);
            endHour = Integer.parseInt(timeRange[1]);
            if (startHour > endHour)
                endHour += 24;
        } catch (Exception e) {
            startHour = 0;
            endHour = 24;
        }
        int check = checkHourRange(startHour, endHour);
        if (!isBatteryAndScreenOk(context) || check != 0) {
            Trace.d("no force update reason : ota battery low or no in hour range");
            ReportData.reportAction(context, "cause_not_right_time");
            scheduleForceUpdate(context, check, startHour, endHour);
        } else {
            Trace.d("time arrive start force update");
            ReportData.reportAction(context, "auto");
            startInstall(context);
        }
    }

    public static void forceReboot(Context context) {
        String[] timeRange = (String[]) QueryInfo.getInstance(context).getPolicy("install_time", String[].class);
        if (PreferencesUtils.getInt(context, "ota_update_status", 0) != 6) {
            ReportData.reportAction(context, "reboot_cause_not_dlcomplete");
            return;
        }
        if ((Boolean) QueryInfo.getInstance(context).getPolicy("install_forced", Boolean.class)) {
            int startHour, endHour;
            try {
                startHour = Integer.parseInt(timeRange[0]);
                endHour = Integer.parseInt(timeRange[1]);
                if (startHour > endHour)
                    endHour += 24;
            } catch (Exception e) {
                startHour = 0;
                endHour = 24;
            }
            int check = checkHourRange(startHour, endHour);
            if (DeviceUtil.getInstance().isScreenOn(context) || check != 0) {
                ReportData.reportAction(context, "reboot_cause_not_right_time");
                scheduleForceReboot(context, check, startHour, endHour);
            } else {
                Trace.d("[force_update] time arrive start force update");
                ReportData.reportAction(context, "auto_reboot");
                Recovery.with(context).reboot();
            }
        } else {
            ReportData.reportAction(context, "cause_not_force_reboot");
        }
    }

    private static boolean isBatteryAndScreenOk(Context context) {
        Intent batteryIntent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int level = batteryIntent.getIntExtra("level", -1);
        int required = (Integer) QueryInfo.getInstance(context).getPolicy("install_battery", Integer.class);
        if (required <= 0)
            required = 30;
        if (level >= required && !DeviceUtil.getInstance().isScreenOn(context)) {
            return true;
        }
        Trace.d("ota_battery low = " + required);
        return false;
    }

    private static int checkHourRange(int start, int end) {
        int current = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        Trace.d("startTime = " + start + "; endTime = " + end + "; hour = " + current);
        if (current < start)
            return -1;
        if (current >= end)
            return 1;
        return 0;
    }

    private static void scheduleForceUpdate(Context context, int check, int start, int end) {
        Calendar calendar = Calendar.getInstance();
        if (check == -1) {
            calendar.set(Calendar.HOUR_OF_DAY, start);
            calendar.set(Calendar.MINUTE, 0);
        } else if (check == 1) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
            calendar.set(Calendar.HOUR_OF_DAY, start);
            calendar.set(Calendar.MINUTE, 0);
        } else {
            calendar.add(Calendar.MINUTE, 10);
        }
        com.foss.fota.update.Alarm.setForceUpdateAlarm(context, calendar.getTimeInMillis());
    }

    private static void scheduleForceReboot(Context context, int check, int start, int end) {
        Calendar calendar = Calendar.getInstance();
        if (check == -1) {
            calendar.set(Calendar.HOUR_OF_DAY, start);
            calendar.set(Calendar.MINUTE, 0);
        } else if (check == 1) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
            calendar.set(Calendar.HOUR_OF_DAY, start);
            calendar.set(Calendar.MINUTE, 0);
        } else {
            calendar.add(Calendar.MINUTE, 10);
        }
        com.foss.fota.update.Alarm.setForceRebootAlarm(context, calendar.getTimeInMillis());
    }

    public static boolean checkFile(Context context) {
        return DeviceUtil.getInstance().isOldReboot();
    }

    public static boolean checkBattery(Context context, int required) {
        Intent batteryIntent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        return batteryIntent.getIntExtra("level", -1) >= required;
    }

    public static boolean checkStorage(Context context, String path) {
        VersionModel versionModel = QueryInfo.getInstance(context).getVersionModel();
        return (versionModel != null && versionModel.getIsOldPkg() == 1)
                || 3 == StorageUtil.getStorageStatus(context, (long) (FileUtil.getFileSize(path) * 1.5d));
    }

    public static void reportInstallResult(Context context) {
        if (PreferencesUtils.getInt(context, "ota_update_status", 0) == 4) {
            Intent intent = new Intent(context, FotaPopInstallWindow.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            context.startActivity(intent);
            long delay = PreferencesUtils.getLong(context, "ota_install_delay_schedule", 0L);
            if (delay > 0) {
                com.foss.fota.update.Alarm.setInstallDelayAlarm(context, delay + System.currentTimeMillis());
            }
        }
    }

    public static void performAutoInstall(Context context) {
        startInstall(context);
    }

    public static boolean isAbUpdateSupported() {
        return isAbUpdate();
    }

    public static boolean isAbInstallSupported(Context context) {
        return isInstallAllowed(context);
    }

    public static boolean isBatteryEnough(Context context, int required) {
        return checkBattery(context, required);
    }

    public static void b(Context context) {
        performInstall(context);
    }

    public static void performInstall(Context context) {
        startInstall(context);
    }

    public static void performLegacyInstall(Context context) {
        startInstall(context);
    }

    public static boolean getType() {
        return isAbUpdate();
    }

    public static boolean a(Context context) {
        return isInstallAllowed(context);
    }

    public static boolean a(Context context, int requiredLevel) {
        Intent batteryStatus = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        if (batteryStatus != null) {
            int level = batteryStatus.getIntExtra("level", -1);
            return level >= requiredLevel;
        }
        return false;
    }

    public static void contentLayout(Context context) {
        startInstall(context);
    }

    public static boolean contentLayout(Context context, String path) {
        // Alias for b(Context, String) in ref which checks space/old pkg
        VersionModel versionModel = QueryInfo.getInstance(context).getVersionModel();
        if (versionModel != null && versionModel.getIsOldPkg() == 1) return true;
        return StorageUtil.isStorageSpaceEnough(context, (long)(FileUtil.getFileSize(path) * 1.5));
    }

    public static boolean a(Context context, String path, boolean unused) {
        return doInstall(context, path);
    }

    public static void d(Context context) {
        com.foss.fota.update.Status.setDownloadCompletedStatus(context);
    }

    public static boolean handlePostDownloadTask(Context context) {
        com.foss.fota.update.Status.handlePostDownloadTask(context);
        return true;
    }
}
