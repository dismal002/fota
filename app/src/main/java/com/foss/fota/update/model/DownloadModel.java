package com.foss.fota.update.model;

/* JADX INFO: loaded from: classes.dex */
public class DownloadModel {
    private long downloadBlockSize;
    private String downloadDir;
    private String downloadFileName;
    private long downloadSimulateTotalSize;
    private int downloadStatus;
    private long downloadTotalSize;
    private String downloadUrl;
    private long rangeEnd;
    private long rangeStart;
    private int retryCount;
    private boolean segmentDownload;
    private String tagFileName;
    private long tagFileSize;
    private String tagId;

    public String getDownloadUrl() {
        return this.downloadUrl;
    }

    public void setDownloadUrl(String str) {
        this.downloadUrl = str;
    }

    public String getDownloadDir() {
        return this.downloadDir;
    }

    public void setDownloadDir(String str) {
        this.downloadDir = str;
    }

    public String getDownloadFileName() {
        return this.downloadFileName;
    }

    public void setDownloadFileName(String str) {
        this.downloadFileName = str;
    }

    public String getTagFileName() {
        return this.tagFileName;
    }

    public void setTagFileName(String str) {
        this.tagFileName = str;
    }

    public String getTagId() {
        return this.tagId;
    }

    public void setTagId(String str) {
        this.tagId = str;
    }

    public long getTagFileSize() {
        return this.tagFileSize;
    }

    public void setTagFileSize(long j) {
        this.tagFileSize = j;
    }

    public long getDownloadTotalSize() {
        return this.downloadTotalSize;
    }

    public void setDownloadTotalSize(long j) {
        this.downloadTotalSize = j;
    }

    public long getDownloadBlockSize() {
        return this.downloadBlockSize;
    }

    public void setDownloadBlockSize(long j) {
        this.downloadBlockSize = j;
    }

    public long getRangeStart() {
        return this.rangeStart;
    }

    public void setRangeStart(long j) {
        this.rangeStart = j;
    }

    public long getRangeEnd() {
        return this.rangeEnd;
    }

    public void setRangeEnd(long j) {
        this.rangeEnd = j;
    }

    public int getRetryCount() {
        return this.retryCount;
    }

    public void setRetryCount(int i) {
        this.retryCount = i;
    }

    public int getDownloadStatus() {
        return this.downloadStatus;
    }

    public void setDownloadStatus(int i) {
        this.downloadStatus = i;
    }

    public boolean isSegmentDownload() {
        return this.segmentDownload;
    }

    public boolean getSegmentDownload() {
        return this.segmentDownload;
    }

    public void setSegmentDownload(boolean z) {
        this.segmentDownload = z;
    }

    public long getDownloadSimulateTotalSize() {
        return this.downloadSimulateTotalSize;
    }

    public void setDownloadSimulateTotalSize(long j) {
        this.downloadSimulateTotalSize = j;
    }

    public String toString() {
        return "DownloadModel{downloadUrl='" + this.downloadUrl + "', downloadDir='" + this.downloadDir + "', downloadFileName='" + this.downloadFileName + "', tagFileName='" + this.tagFileName + "', tagId='" + this.tagId + "', tagFileSize=" + this.tagFileSize + ", downloadTotalSize=" + this.downloadTotalSize + ", downloadBlockSize=" + this.downloadBlockSize + ", rangeStart=" + this.rangeStart + ", rangeEnd=" + this.rangeEnd + ", retryCount=" + this.retryCount + ", downloadStatus=" + this.downloadStatus + ", segmentDownload=" + this.segmentDownload + ", downloadSimulateTotalSize=" + this.downloadSimulateTotalSize + '}';
    }
}
