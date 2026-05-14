package com.foss.fota.update.download;

import android.content.Context;
import android.os.Build;
import androidx.core.view.PointerIconCompat;
import com.foss.fota.MyJobService;
import com.foss.fota.update.model.VersionModel;
import com.foss.fota.utils.Trace;
import com.foss.fota.utils.NetWorkUtil;
import com.foss.fota.utils.NotifyManager;
import com.foss.fota.utils.OkHttpUtil;
import com.foss.fota.utils.PreferencesUtils;
import com.foss.fota.utils.StorageUtil;
import com.foss.fota.update.query.QueryInfo;
import com.foss.fota.update.report.ReportData;
import de.greenrobot.event.EventBus;
import java.io.File;

/* JADX INFO: compiled from: DownPackage.java */
/* JADX INFO: loaded from: classes.dex */
public class DownPackage extends Download {
    private static DownPackage instance;
    private VersionModel versionModel;
    private DownTask downTask;

    public DownPackage(Context context) {
        super(context);
    }

    public static DownPackage getInstance(Context context) {
        if (instance == null) {
            synchronized (DownPackage.class) {
                if (instance == null) {
                    instance = new DownPackage(context);
                }
            }
        }
        return instance;
    }

    @Override // com.foss.fota.update.download.Download
    public void start() {
        this.versionModel = QueryInfo.getInstance(this.context).getVersionModel();
        if (this.versionModel == null) {
            Trace.d("versionModel is null");
            onDownloadFailed(0, "versionModel is null");
            return;
        }
        synchronized (this) {
            String downloadPath = StorageUtil.getDownloadPath(this.context);
            boolean isPackageExist = new File(downloadPath + "/update.zip").exists();
            Trace.d("package size = " + this.versionModel.getFilesize() + "; package url = " + this.versionModel.getDeltaurl() + "; package version = " + this.versionModel.getVersionName() + "; package md5 = " + this.versionModel.getMd5sum() + "; isPkgExist = " + isPackageExist);
            
            if (isDownloading()) {
                ReportData.reportAction(this.context, "cause_downloading");
                return;
            }
            
            ReportData.reportAction(this.context, isPackageExist ? "resume" : "download");
            PreferencesUtils.putString(this.context, "update_package_path", downloadPath);
            
            try {
                this.downTask = new DownTask();
                this.downTask.setDir(downloadPath)
                        .setTotalSize(this.versionModel.getFilesize())
                        .setRetryCount(5)
                        .setTagId("DownloadPackage")
                        .setListener(new IDownloadListener() {
                            @Override
                            public void onStart(String str) {
                                Trace.d("downloading package start : " + str);
                            }

                            @Override
                            public void onFailure(String str, int errorCode, String message) {
                                Trace.d("downloading package fail = " + errorCode + "; message = " + message);
                                DownPackage.this.onDownloadFailed(errorCode, message);
                                if (errorCode != 3011) {
                                    ReportData.reportDownload(DownPackage.this.context, "cause_fail#" + message, DownPackage.this.getDuration());
                                    OkHttpUtil.clearDnsCache();
                                }
                            }

                            @Override
                            public void onProgress(String str, long currentSize, long totalSize) {
                                DownPackage.this.onDownloadProgress(currentSize, totalSize);
                            }

                            @Override
                            public void onSuccess(String str, String str2) {
                                Trace.d("downloading package success");
                                DownPackage.this.onDownloadCompleted();
                            }
                        });
                this.downTask.start(this.versionModel.getDeltaurl());
                startTimer();
            } catch (Exception e) {
                e.printStackTrace();
                onDownloadFailed(0, e.toString());
                ReportData.reportAction(this.context, "cause_exception#" + e.getMessage());
            }
            
            NetWorkUtil.updateNetworkStatus(this.context);
            if (Build.VERSION.SDK_INT >= 21) {
                MyJobService.schedule(this.context, 1000, 300000L, 1440 * MyJobService.INTERVAL_UNIT);
            }
        }
    }

    public void pause() {
        if (this.downTask == null) return;
        this.downTask.stop();
        Trace.d("STATE_PAUSEDOWNLOAD");
        PreferencesUtils.putInt(this.context, "ota_update_status", 3);
        ReportData.reportAction(this.context, "cause_model_null");
    }

    @Override // com.foss.fota.update.download.Download
    public void stop() {
        super.stop();
        if (this.downTask != null) {
            this.downTask.stop();
            ReportData.reportDownload(this.context, "pause", getDuration());
        }
    }

    @Override // com.foss.fota.update.download.Download
    public void cancel() {
        super.cancel();
        if (this.downTask != null) {
            this.downTask.cancel();
            ReportData.reportDownload(this.context, "cancel", getDuration());
            StorageUtil.clearDownloadDir(this.context, StorageUtil.getDownloadPath(this.context));
        }
    }

    public boolean isDownloading() {
        return this.downTask != null && this.downTask.isRunning();
    }

    public void onDownloadCompleted() {
        resetTimer();
        Trace.d("download is completed");
        com.foss.fota.update.Status.setDownloadCompletedStatus(this.context);
        ReportData.reportDownload(this.context, "finish", getDuration());
        com.foss.fota.update.Status.performAutoInstall(this.context);
    }

    public void onDownloadFailed(int errorCode, String message) {
        Trace.d("STATE_PAUSEDOWNLOAD,pkg download fail reason : " + message);
        PreferencesUtils.putInt(this.context, "ota_update_status", 3);
        EventBus.getDefault().post(new com.foss.fota.update.EventMessage(200, 3000, errorCode, 0L, message));
        com.foss.fota.update.Notice.showUpdateNotice(this.context);
    }
    public void onDownloadProgress(long currentSize, long totalSize) {
        EventBus.getDefault().post(new com.foss.fota.update.EventMessage(200, 3001, currentSize, totalSize, null));
    }

    public void startTimer() {
        this.startTime = System.currentTimeMillis();
    }
}
