package com.foss.fota.update.model;

import java.util.ArrayList;
import java.util.List;

public class VersionModel {
    private long deltaId;
    private String deltaurl;
    private long filesize;
    private int isOldPkg;
    private int issilent;
    private String md5sum;
    private List<PolicyModel> policy;
    private String release_date;
    private ArrayList<LanguageModel> releasenotes;
    private String resVersionName;
    private ArrayList<SegmentModel> segmentSha;
    private String sha;
    private int upsuccflag;
    private String versionName;

    public int getUpsuccflag() {
        return this.upsuccflag;
    }

    public void setUpsuccflag(int i) {
        this.upsuccflag = i;
    }

    public int getIssilent() {
        return this.issilent;
    }

    public void setIssilent(int i) {
        this.issilent = i;
    }

    public String getSourcename() {
        return this.resVersionName;
    }

    public void setSourcename(String str) {
        this.resVersionName = str;
    }

    public int getIsOldPkg() {
        return this.isOldPkg;
    }

    public void setIsOldPkg(int i) {
        this.isOldPkg = i;
    }

    public ArrayList<SegmentModel> getSegmentInfo() {
        return this.segmentSha;
    }

    public void setSegmentInfo(ArrayList<SegmentModel> arrayList) {
        this.segmentSha = arrayList;
    }

    public String getVersionName() {
        return this.versionName;
    }

    public void setVersionName(String str) {
        this.versionName = str;
    }

    public String getDeltaurl() {
        return this.deltaurl;
    }

    public void setDeltaurl(String str) {
        this.deltaurl = str;
    }

    public String getMd5sum() {
        return this.md5sum;
    }

    public void setMd5sum(String str) {
        this.md5sum = str;
    }

    public long getDeltaId() {
        return this.deltaId;
    }

    public void setDeltaId(long j) {
        this.deltaId = j;
    }

    public long getFilesize() {
        return this.filesize;
    }

    public void setFilesize(long j) {
        this.filesize = j;
    }

    public String getRelease_date() {
        return this.release_date;
    }

    public void setRelease_date(String str) {
        this.release_date = str;
    }

    public List<PolicyModel> getPolicy() {
        return this.policy;
    }

    public void setPolicy(List<PolicyModel> list) {
        this.policy = list;
    }

    public ArrayList<LanguageModel> getReleasenotes() {
        return this.releasenotes;
    }

    public void setReleasenotes(ArrayList<LanguageModel> arrayList) {
        this.releasenotes = arrayList;
    }

    public String getSha() {
        return this.sha;
    }
}
