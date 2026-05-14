package com.foss.fota.config;

/* JADX INFO: compiled from: ServerApi.java */
/* JADX INFO: loaded from: classes.dex */
public class ServerApi {
    public static final String PRIMARY_DOMAIN = "https://fota5t.foss.com";
    public static final String SECONDARY_DOMAIN = "https://fota5t.foss.cn";
    public static final String REPORT_DOMAIN = "https://fruet.foss.com";
    public static final String API_PATH = "/otainter-5.0/fota5/";
    public static final String QUERY_ENDPOINT = API_PATH + "detectSchedule.do";
    public static final String FULL_QUERY_ENDPOINT = API_PATH + "fullDetectSchedule.do";
    public static final String REPORT_ENDPOINT = API_PATH + "submitReport.do";
    public static final String STATUS_REPORT_URL = REPORT_DOMAIN + "/euft/repsta";
}
