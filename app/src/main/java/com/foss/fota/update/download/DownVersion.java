package com.foss.fota.update.download;

import android.content.Context;
import android.os.Build;
import android.os.Process;
import com.foss.fota.JobServiceUtil.TaskIntentJobService;
import com.foss.fota.TaskIntentService;
import com.foss.fota.update.EventMessage;
import com.foss.fota.update.Notice;
import com.foss.fota.update.Status;
import com.foss.fota.update.query.QueryInfo;
import com.foss.fota.update.report.ReportData;
import com.foss.fota.utils.DeviceUtil;
import com.foss.fota.utils.IntentUtil;
import com.foss.fota.utils.NetWorkUtil;
import com.foss.fota.utils.PreferencesUtils;
import com.foss.fota.utils.StorageUtil;
import com.foss.fota.utils.Trace;
import de.greenrobot.event.EventBus;
import com.foss.fota.update.model.VersionModel;

public class DownVersion {
    private static DownTask currentDownloadTask;
    private static DownVersion instance;
    private Context context;
    private int downloadType = 0;

    private DownVersion(Context context) {
        this.context = context.getApplicationContext();
    }

    public static DownVersion getInstance(Context context) {
        if (instance == null) {
            synchronized (DownVersion.class) {
                if (instance == null) {
                    instance = new DownVersion(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    public void startDownload(int type) {
        synchronized (DownVersion.class) {
            if (currentDownloadTask != null) {
                int status = PreferencesUtils.getInt(this.context, "ota_update_status", 0);
                Trace.d("version_status = " + status);
                if (status == 3) {
                    currentDownloadTask = null;
                } else {
                    Trace.d("downloading package ");
                    ReportData.reportAction(this.context, "cause_downloading");
                    return;
                }
            } else {
                Trace.d("mDown == null; flag = " + type);
            }
            PreferencesUtils.putInt(this.context, "ota_update_status", 2);
            if (type == 1) {
                Trace.d("download AUTO");
            }
            this.downloadType = type;
            performDownload();
        }
    }

    // Compatibility overload used by some older call sites.
    public void startDownload(VersionModel versionModel) {
        if (versionModel != null) {
            QueryInfo.getInstance(this.context).setVersionModel(this.context, versionModel);
        }
        startDownload(0);
    }

    public int getDownloadType() {
        return this.downloadType;
    }


    private void performDownload() {
        Trace.d("downloadPackage begin");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Process.setThreadPriority(10);
                    if (DownVersion.currentDownloadTask == null) {
                        Trace.d("download package start, normal update ");
                        DownVersion.currentDownloadTask = DownTask.getInstance(DownVersion.this.context);
                        DownVersion.currentDownloadTask.start();
                    } else {
                        Trace.d("download package, downloading");
                    }
                } catch (Exception e) {
                    if (DownVersion.currentDownloadTask != null) {
                        DownVersion.currentDownloadTask.cancel();
                        DownVersion.currentDownloadTask = null;
                    }
                    Trace.d("download package, Exception = " + e.getMessage());
                    PreferencesUtils.putInt(DownVersion.this.context, "ota_update_status", 3);
                    ReportData.reportAction(DownVersion.this.context, "cause_start_exception " + e.getMessage());
                }
            }
        }).start();
    }

    public void stopDownload() {
        if (this.context == null) {
            Trace.d("mContext is null");
            return;
        }
        synchronized (this) {
            try {
                Trace.d("");
                if (currentDownloadTask != null) {
                    currentDownloadTask.stop();
                    currentDownloadTask = null;
                }
                QueryInfo.getInstance(this.context).reset(this.context);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void cancelDownload() {
        if (this.context == null) {
            Trace.d("mContext is null");
            return;
        }
        synchronized (this) {
            Trace.d("");
            if (currentDownloadTask != null) {
                currentDownloadTask.cancel();
                currentDownloadTask = null;
            }
            StorageUtil.deleteUpdatePackage(this.context);
            Notice.cancelUpdate(this.context);
        }
    }

    public void onDownloadStatusChanged() {
        synchronized (this) {
            Status.cancelUpdate(this.context);
        }
    }

    private boolean isAutoDownloadSatisfied() {
        boolean isWifi = NetWorkUtil.isWifiConnected(this.context);
        int status = PreferencesUtils.getInt(this.context, "ota_update_status", 0);
        if (isDownloadFailedTooManyTimes()) {
            return false;
        }
        boolean canAutoDown = canAutoDownload(isWifi, false);
        Trace.d("isAutoDown= " + canAutoDown + "; version_status= " + status);
        return (status == 1 || status == 3) && canAutoDown;
    }

    public boolean canAutoDownload(boolean isWifi, boolean extra) {
        try {
            int autoDownloadPolicy = ((Integer) QueryInfo.getInstance(this.context).getPolicy("download_auto", Integer.class)).intValue();
            Trace.d("autoDownload = " + autoDownloadPolicy);
            if (autoDownloadPolicy == 2) {
                return false;
            }
            if (autoDownloadPolicy == 1) {
                return isNetworkSatisfied(isWifi);
            }
            if (autoDownloadPolicy == 0) {
                return isUserAllowedAutoDownload(isWifi, extra);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    private boolean isNetworkSatisfied(boolean isWifi) {
        boolean isForced = ((Boolean) QueryInfo.getInstance(this.context).getPolicy("install_forced", Boolean.class)).booleanValue();
        boolean isWifiOnly = ((Boolean) QueryInfo.getInstance(this.context).getPolicy("download_wifi", Boolean.class)).booleanValue();
        boolean autoDownload = ((Boolean) QueryInfo.getInstance(this.context).getPolicy("download_auto", Boolean.class)).booleanValue();
        Trace.d("isForced = " + isForced + "; isForcedWifi = " + isWifiOnly + "; autoDownload = " + autoDownload);
        if ((isForced || autoDownload) && !isWifiOnly) {
            return true;
        }
        return (isForced || autoDownload) && isWifiOnly && isWifi;
    }

    public boolean isUserAllowedAutoDownload(boolean isWifi, boolean isNetChange) {
        boolean wifiAuto = PreferencesUtils.getBoolean(this.context, "download_wifi_auto", DeviceUtil.getInstance().isAutoWifiEnabled());
        boolean onlyWifi = PreferencesUtils.getBoolean(this.context, "download_only_wifi", DeviceUtil.getInstance().isWifiOnlyEnabled());
        int status = PreferencesUtils.getInt(this.context, "ota_update_status", 0);
        Trace.d("isWifi = " + isWifi + "; isWifiAuto = " + wifiAuto + "; isOnlyWifi = " + onlyWifi);
        if (isNetChange || status == 2) {
            if (!isWifi) {
                return false;
            }
        } else {
            if (!wifiAuto) {
                return false;
            }
            if (wifiAuto && !isWifi) {
                return false;
            }
        }
        return true;
    }

    public boolean isDownloadFailedTooManyTimes() {
        try {
            int failCount = PreferencesUtils.getInt(this.context, "ota_install_fail_count", 0);
            if (failCount >= 5) {
                if (com.foss.fota.update.report.ReportManager.getInstance(this.context).isReportScheduled("cause_install_fail_5", 21600000L)) {
                    ReportData.reportAction(this.context, "cause_install_fail_5");
                }
                Trace.d("updatefailcount = " + failCount + ", return true!!!");
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public void checkAndStartAutoDownload() {
        try {
            int storageStatus = StorageUtil.checkStorage(this.context, QueryInfo.getInstance(this.context).getVersionModel().getFilesize());
            if (storageStatus == 1 || storageStatus == 2) {
                Trace.d("sdcard is not available, return");
                if (com.foss.fota.update.report.ReportManager.getInstance(this.context).isReportScheduled("down_status_cause_not_enough", 21600000L)) {
                    ReportData.reportAction(this.context, "cause_not_enough");
                }
                return;
            }
        } catch (Exception e) {
        }
        if (isAutoDownloadSatisfied()) {
            startDownload(1);
            return;
        }
        Trace.d("no download reason : not satisfy auto download condition");
        if (com.foss.fota.update.Status.getUpdateStatus(this.context) == 0 && com.foss.fota.update.report.ReportManager.getInstance(this.context).isReportScheduled("down_status_cause_unauto", 21600000L)) {
            ReportData.reportAction(this.context, "cause_unauto");
        }
    }

    public boolean isDownloading() {
        return currentDownloadTask != null && currentDownloadTask.isDownloading() && PreferencesUtils.getInt(this.context, "ota_update_status", 0) == 2;
    }

    public void onNetworkChanged(Context context) {
        if (QueryInfo.getInstance(this.context).getVersionModel() != null) {
            if (DownVersion.getInstance(this.context).isDownloading()) {
                boolean canAuto = canAutoDownload(NetWorkUtil.isWifiConnected(this.context), true);
                Trace.d("isAutoDown= " + canAuto);
                if (!canAuto) {
                    ReportData.reportAction(this.context, "cause_net_change_downloading");
                    stopDownload();
                    EventBus.getDefault().post(new EventMessage(200, 5001, 0L, 0L, null));
                    return;
                }
                return;
            }
            if (Build.VERSION.SDK_INT >= 24) {
                IntentUtil.a(context, 3, TaskIntentJobService.class, 9, 1, "");
            } else {
                TaskIntentService.a(context, 9, 1, "");
            }
        }
    }
}
