package com.foss.fota.update.download;

import android.content.Context;
import android.os.SystemClock;
import androidx.core.view.PointerIconCompat;
import android.text.TextUtils;
import com.foss.fota.MyApplication;
import com.foss.fota.update.model.DownloadModel;
import com.foss.fota.update.model.SegmentModel;
import com.foss.fota.update.model.VersionModel;
import com.foss.fota.utils.Trace;
import com.foss.fota.utils.FileUtil;
import com.foss.fota.utils.NetWorkUtil;
import com.foss.fota.utils.OkHttpUtil;
import com.foss.fota.utils.PreferencesUtils;
import com.foss.fota.utils.SecurityUtil;
import com.foss.fota.update.query.QueryInfo;
import com.squareup.okhttp.Request;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class DownTask {
    private IDownloadListener listener;
    private HashMap<String, String> headers;
    private DownloadModel downloadModel;
    private VersionModel versionModel;
    private ArrayList<SegmentModel> segments;
    private static DownTask instance;

    public static DownTask getInstance(Context context) {
        if (instance == null) {
            instance = new DownTask();
        }
        return instance;
    }

    public void start() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    execute();
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void start(String url) {
        setUrl(url);
        start();
    }

    public static void stopAllTasks() {
        if (instance != null) {
            instance.stop();
        }
    }

    public boolean isRunning() {
        return isDownloading();
    }

    public DownTask() {
        init();
    }

    public static void clearProgress() {
        PreferencesUtils.putInt(MyApplication.getInstance(), "segment_number", 0);
        PreferencesUtils.putLong(MyApplication.getInstance(), "segment_number_fail_simulate_size", 0L);
    }

    public void init() {
        Trace.d(getClass().getSimpleName());
        this.downloadModel = null;
        this.downloadModel = new DownloadModel();
        this.downloadModel.setDownloadBlockSize(20971520L);
        this.downloadModel.setRetryCount(3);
        this.downloadModel.setDownloadDir("/sdcard/");
        this.downloadModel.setTagFileName(null);
        this.downloadModel.setDownloadFileName(null);
        this.downloadModel.setTagId(null);
        this.downloadModel.setDownloadTotalSize(0L);
        this.downloadModel.setTagFileSize(0L);
        this.downloadModel.setSegmentDownload(false);
        this.downloadModel.setDownloadSimulateTotalSize(0L);
        this.downloadModel.setDownloadStatus(0);
    }

    public DownTask setTagId(String str) {
        this.downloadModel.setTagId(str);
        return this;
    }

    private boolean isSegmentDownload() {
        return this.downloadModel.getSegmentDownload();
    }

    public DownTask setUrl(String str) {
        this.downloadModel.setDownloadUrl(str);
        return this;
    }

    public DownTask setDir(String str) {
        this.downloadModel.setDownloadDir(str);
        return this;
    }

    public DownTask setFileName(String str) {
        this.downloadModel.setDownloadFileName(str);
        return this;
    }

    public DownTask setRetryCount(int i) {
        this.downloadModel.setRetryCount(i);
        return this;
    }

    public DownTask setTotalSize(long j) {
        this.downloadModel.setTagFileSize(j);
        return this;
    }

    public DownTask setListener(IDownloadListener listener) {
        this.listener = listener;
        return this;
    }

    public void stop() {
        Trace.d(getClass().getSimpleName());
        this.downloadModel.setDownloadStatus(2);
    }

    public void cancel() {
        Trace.d(getClass().getSimpleName());
        this.downloadModel.setDownloadStatus(0);
    }

    public boolean isDownloading() {
        return this.downloadModel.getDownloadStatus() == 1;
    }

    private void backupFile(String str) {
        if (!TextUtils.isEmpty(str)) {
            FileUtil.b(this.downloadModel.getDownloadDir() + this.downloadModel.getDownloadFileName(), str);
        }
    }

    public boolean execute() throws Throwable {
        long j;
        int i;
        boolean z = true;
        File file = new File(this.downloadModel.getDownloadDir());
        if (!file.exists()) {
            file.mkdirs();
        }
        if (!file.exists()) {
            notifyEvent(3000, 3003, MyApplication.getInstance().getString(com.foss.fota.R.string.sdcard_crash_dir_un_build));
            Trace.d(this.downloadModel.getDownloadDir() + " is illness");
            return true;
        }
        if (this.downloadModel.getTagFileSize() <= 0) {
            this.downloadModel.setTagFileSize(getFileSize(this.downloadModel.getDownloadUrl()));
        }
        Trace.d("tag_file_size = " + this.downloadModel.getTagFileSize());
        if (this.downloadModel.getTagFileSize() <= 0) {
            notifyEvent(3000, 3001, "RESPONSE_ERROR");
            return false;
        }
        if (TextUtils.isEmpty(this.downloadModel.getTagFileName())) {
            this.downloadModel.setTagFileName(this.downloadModel.getDownloadFileName());
        }
        long jE = FileUtil.e(this.downloadModel.getDownloadDir() + this.downloadModel.getDownloadFileName());
        if (isSegmentDownload()) {
            this.versionModel = QueryInfo.getInstance(MyApplication.getInstance()).getVersionModel();
            this.segments = this.versionModel.getSegmentInfo();
            if (jE > 0) {
                int iB = PreferencesUtils.b(MyApplication.getInstance(), "segment_number", 0);
                setFileName("/update" + iB + ".zip");
                long jE2 = FileUtil.e(this.downloadModel.getDownloadDir() + this.downloadModel.getDownloadFileName());
                if (!this.downloadModel.getDownloadFileName().equalsIgnoreCase("/update.zip")) {
                    jE += jE2;
                }
                j = jE;
                i = iB;
            } else {
                PreferencesUtils.putInt(MyApplication.getInstance(), "segment_number", 0);
                setFileName("/update.zip");
                j = jE;
                i = 0;
            }
            Trace.d("segmentNumber = " + i + "; download_total_size = " + j);
            long jB = PreferencesUtils.b(MyApplication.getInstance(), "segment_number_fail_simulate_size", -1L);
            if (jB < j) {
                jB = j;
            }
            this.downloadModel.setDownloadSimulateTotalSize(jB);
        } else {
            j = jE;
        }
        Trace.d("download_total_size = " + j);
        this.downloadModel.setDownloadTotalSize(j);
        if (this.downloadModel.getDownloadTotalSize() > 0 && this.downloadModel.getDownloadTotalSize() >= this.downloadModel.getTagFileSize()) {
            if (!TextUtils.isEmpty(this.downloadModel.getTagFileName()) && !this.downloadModel.getTagFileName().equals(this.downloadModel.getDownloadFileName())) {
                backupFile(this.downloadModel.getDownloadDir() + this.downloadModel.getTagFileName());
            }
            notifyEvent(2000, 0, null);
            notifyEvent(PointerIconCompat.TYPE_CONTEXT_MENU, 0, "");
            return true;
        }
        notifyEvent(1000, 0, "DOWNLOAD_START");
        int i2 = 0;
        while (i2 < this.downloadModel.getRetryCount()) {
            if (!isDownloading()) {
                notifyEvent(3000, 3011, "PAUSE");
                return false;
            }
            i2++;
            Trace.d("retry times:" + i2);
            try {
                if (performDownload()) {
                    break;
                }
            } catch (Throwable e) {
                if (e instanceof DownloadException) {
                    DownloadException de = (DownloadException) e;
                    Trace.d("DownloadException code = " + de.getCode() + "; message = " + de.getMessage());
                } else {
                    Trace.d("Download error: " + e.getMessage());
                }
                if (i2 >= this.downloadModel.getRetryCount() || !NetWorkUtil.a(MyApplication.getInstance())) {
                    break;
                }
                if (this.downloadModel.getDownloadStatus() != 0) {
                    SystemClock.sleep(3000L);
                } else {
                    return z;
                }
            }
        }
        if (this.downloadModel.getDownloadTotalSize() < this.downloadModel.getTagFileSize()) {
            Trace.d("download didn't finish");
            notifyEvent(3000, 3012, "UNDONE");
        }
        return this.downloadModel.getDownloadTotalSize() == this.downloadModel.getTagFileSize() ? z : false;
    }

    private boolean checkFileExists() {
        if (!new File(this.downloadModel.getDownloadDir() + "/update.zip").exists()) {
            Trace.d("update.zip is not exist");
            this.downloadModel.setDownloadFileName("/update.zip");
            this.downloadModel.setDownloadTotalSize(0L);
            PreferencesUtils.putInt(MyApplication.getInstance(), "segment_number", 0);
        }
        return false;
    }

    private boolean verifySha256() throws Throwable {
        if (checkFileExists()) {
            return false;
        }
        Context contextA = MyApplication.getInstance();
        String strB = SecurityUtil.b(this.downloadModel.getDownloadDir() + this.downloadModel.getDownloadFileName());
        int iB = PreferencesUtils.b(contextA, "segment_number", 0);
        if (strB == null || !strB.equalsIgnoreCase(this.segments.get(iB).getKey())) {
            PreferencesUtils.putInt(contextA, "segment_number_fail", PreferencesUtils.b(contextA, "segment_number_fail", 0) + 1);
            Trace.d("file sha256  = " + strB + "; server_sha256=" + this.segments.get(iB).getKey() + "; segmentNumber=" + iB);
            FileUtil.f(this.downloadModel.getDownloadDir() + this.downloadModel.getDownloadFileName());
            this.downloadModel.setDownloadTotalSize(this.segments.get(iB).getStartdec());
            if (PreferencesUtils.b(contextA, "segment_number_fail", 0) > 4) {
                Trace.d("sha256 is different 5 times, delete package and report");
                com.foss.fota.update.report.ReportData.reportAction(contextA, "cause_sha256");
                PreferencesUtils.putInt(contextA, "segment_number_fail", 0);
                notifyEvent(3000, 0, contextA.getString(com.foss.fota.R.string.package_unzip_error));
                stop();
                com.foss.fota.update.Status.c(contextA);
            }
            PreferencesUtils.putBoolean(contextA, "segment_number_fail_simulate_size", this.downloadModel.getDownloadSimulateTotalSize());
            return false;
        }
        PreferencesUtils.putLong(contextA, "segment_number_fail_simulate_size", 0L);
        PreferencesUtils.putInt(contextA, "segment_number_fail", 0);
        return true;
    }

    /* JADX WARN: Removed duplicated region for block: B:209:0x0162 A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:48:0x0167 A[Catch: IOException -> 0x0488, TRY_LEAVE, TryCatch #10 {IOException -> 0x0488, blocks: (B:46:0x0162, B:48:0x0167), top: B:209:0x0162 }] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    private boolean performDownload() throws java.lang.Throwable {
        /*
            Method dump skipped, instruction units count: 1244
            To view this dump change 'Code comments level' option to 'DEBUG'
        */
        throw new UnsupportedOperationException("Method not decompiled: com.foss.fota.update.download.DownTask.j():boolean");
    }

    private boolean isFinished() {
        if (!isSegmentDownload()) {
            Trace.d("mFile.length() = " + new File(this.downloadModel.getDownloadDir() + this.downloadModel.getDownloadFileName()).length());
            return true;
        }
        Trace.d("");
        try {
            if (verifySha256() && FileUtil.c(this.downloadModel.getDownloadDir() + this.downloadModel.getDownloadFileName(), this.downloadModel.getDownloadDir() + "/update.zip")) {
                Trace.d("download success");
                return true;
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return false;
    }

    private long getFileSize(String str) {
        try {
            return OkHttpUtil.a(new Request.Builder().url(str).get().build()).body().contentLength();
        } catch (Exception e) {
            e.printStackTrace();
            return -1L;
        }
    }

    private void notifyEvent(int i, int i2, String str) {
        if (this.listener != null) {
            switch (i) {
                case 1000:
                    Trace.d("DOWNLOAD_START getTagId = " + this.downloadModel.getTagId());
                    this.downloadModel.setDownloadStatus(1);
                    this.downloadModel.setDownloadSimulateTotalSize(0L);
                    this.listener.onStart(this.downloadModel.getTagId());
                    break;
                case PointerIconCompat.TYPE_CONTEXT_MENU /* 1001 */:
                    Trace.d("DOWNLOAD_SUCCESS getTagId = " + this.downloadModel.getTagId() + ";  filePath = " + this.downloadModel.getDownloadDir() + this.downloadModel.getTagFileName());
                    this.downloadModel.setDownloadStatus(0);
                    this.listener.onSuccess(this.downloadModel.getTagId(), this.downloadModel.getDownloadDir() + this.downloadModel.getTagFileName());
                    break;
                case 2000:
                    if (this.downloadModel.getTagFileSize() < this.downloadModel.getDownloadTotalSize()) {
                        this.downloadModel.setDownloadTotalSize(this.downloadModel.getTagFileSize());
                    }
                    if (this.downloadModel.getDownloadSimulateTotalSize() < this.downloadModel.getDownloadTotalSize()) {
                        this.downloadModel.setDownloadSimulateTotalSize(this.downloadModel.getDownloadTotalSize());
                    }
                    Trace.d("DOWNLOAD_PROGRESS getTagId = " + this.downloadModel.getTagId() + ";  getTagFileSize = " + this.downloadModel.getTagFileSize() + ";  getDownloadTotalSize = " + this.downloadModel.getDownloadTotalSize() + "; getDownloadSimulateTotalSize = " + this.downloadModel.getDownloadSimulateTotalSize());
                    this.listener.onProgress(this.downloadModel.getTagId(), this.downloadModel.getTagFileSize(), this.downloadModel.getDownloadSimulateTotalSize());
                    break;
                case 3000:
                    Trace.d("DOWNLOAD_FAIL getTagId = " + this.downloadModel.getTagId() + "; status = " + i2 + "; error = " + str);
                    if (this.downloadModel.getDownloadStatus() != 0) {
                        if (i2 != 3011) {
                            this.downloadModel.setDownloadStatus(0);
                        }
                        this.listener.onFailure(this.downloadModel.getTagId(), i2, str);
                    }
                    break;
            }
        }
    }
}
