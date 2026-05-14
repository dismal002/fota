package com.foss.fota.update.download;

import android.content.Context;
import com.foss.fota.utils.Trace;

/* JADX INFO: compiled from: Download.java */
/* JADX INFO: loaded from: classes.dex */
public class Download {
    protected Context context;
    protected long startTime;
    protected long endTime;

    public Download(Context context) {
        this.context = context;
    }

    public void start() {
        Trace.d(getClass().getSimpleName());
        this.startTime = System.currentTimeMillis();
    }

    public void stop() {
        Trace.d(getClass().getSimpleName());
        resetTimer();
    }

    public void cancel() {
        Trace.d(getClass().getSimpleName());
        resetTimer();
    }

    public boolean isDownloading() {
        return false;
    }

    public void resetTimer() {
        this.endTime = System.currentTimeMillis();
    }

    public long getDuration() {
        long duration = (this.endTime - this.startTime) / 1000;
        if (duration > 0) {
            return duration;
        }
        return 0L;
    }
}
