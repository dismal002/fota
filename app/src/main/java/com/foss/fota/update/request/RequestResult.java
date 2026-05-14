package com.foss.fota.update.request;

public class RequestResult {
    private boolean success;
    private int httpCode;
    private int errorCode;
    private String errorMessage = "";
    private String content = "";
    private long startTime = 0;
    private long endTime = 0;

    public int getErrorCode() {
        return this.errorCode;
    }

    public RequestResult setErrorCode(int errorCode) {
        this.errorCode = errorCode;
        return this;
    }

    public String getErrorMessage() {
        return this.errorMessage;
    }

    public RequestResult setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        return this;
    }

    public boolean isSuccess() {
        return this.success;
    }

    public RequestResult setSuccess(boolean success) {
        this.success = success;
        return this;
    }

    public String getContent() {
        return this.content;
    }

    public RequestResult setContent(String content) {
        this.content = content;
        return this;
    }

    public int getHttpCode() {
        return this.httpCode;
    }

    public RequestResult setHttpCode(int httpCode) {
        this.httpCode = httpCode;
        return this;
    }

    public long getStartTime() {
        return this.startTime;
    }

    public RequestResult setStartTime(long startTime) {
        this.startTime = startTime;
        return this;
    }

    public long getEndTime() {
        return this.endTime;
    }

    public RequestResult setEndTime(long endTime) {
        this.endTime = endTime;
        return this;
    }

    // Compatibility aliases for older call sites / decompiled sources.
    public int initData() { return getHttpCode(); }
    public int getType() { return getErrorCode(); }
    public String getStatus() { return getErrorMessage(); }
    public boolean c() { return isSuccess(); }
    public String getData() { return getContent(); }
}
