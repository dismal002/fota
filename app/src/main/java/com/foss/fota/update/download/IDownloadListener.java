package com.foss.fota.update.download;

/* JADX INFO: compiled from: IDownloadListener.java */
/* JADX INFO: loaded from: classes.dex */
public interface IDownloadListener {
    void onStart(String str);

    void onFailure(String str, int i, String str2);

    void onProgress(String str, long j, long j2);

    void onSuccess(String str, String str2);
}
