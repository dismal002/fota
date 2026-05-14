package com.foss.fota.update;

/* JADX INFO: compiled from: EventMessage.java */
/* JADX INFO: loaded from: classes.dex */
public class EventMessage {
    private int type;
    private int status;
    private long arg1;
    private long arg2;
    private Object data;

    public EventMessage(int type, int status, long arg1, long arg2, Object data) {
        this.type = type;
        this.status = status;
        this.arg1 = arg1;
        this.arg2 = arg2;
        this.data = data;
    }

    public int getType() {
        return this.type;
    }

    public int getStatus() {
        return this.status;
    }

    public long getArg1() {
        return this.arg1;
    }

    public long getArg2() {
        return this.arg2;
    }

    public Object getData() {
        return this.data;
    }

    // Compatibility aliases
    public int c() { return (int) this.arg1; }
    public long d() { return this.arg2; }
    public Object initData() { return this.data; }
}
