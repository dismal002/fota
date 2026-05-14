package com.foss.fota.update.model;

public class SegmentModel {
    private long enddec;
    private String key;
    private int number;
    private long startdec;

    public long getEnddec() {
        return this.enddec;
    }

    public void setEnddec(long j) {
        this.enddec = j;
    }

    public long getStartdec() {
        return this.startdec;
    }

    public void setStartdec(long j) {
        this.startdec = j;
    }

    public int getNumber() {
        return this.number;
    }

    public void setNumber(int i) {
        this.number = i;
    }

    public String getKey() {
        return this.key;
    }

    public void setKey(String str) {
        this.key = str;
    }
}
