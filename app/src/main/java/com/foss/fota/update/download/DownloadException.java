package com.foss.fota.update.download;

import com.foss.fota.utils.Trace;

public class DownloadException extends Exception {
    private static final long serialVersionUID = 12874823278L;
    private int code;
    private String message;

    public DownloadException(int i, String str) {
        super(str);
        this.code = i;
        this.message = str;
        Trace.d(str);
    }

    public int getCode() {
        return this.code;
    }

    @Override // java.lang.Throwable
    public String getMessage() {
        return this.message;
    }
}
