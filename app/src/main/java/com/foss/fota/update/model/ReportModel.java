package com.foss.fota.update.model;

/* JADX INFO: loaded from: classes.dex */
public class ReportModel {

    public static class ReportResult {
        public String action;
        public String result;
        public long time;
    }

    public static class RQuery {
        String action;
        RQueryData data;

        public static class RQueryData {
            public int apn;
            public int check_type;
            public String errCode;
            public String reason;
            public int status;
            public String time;
            public int type;
            public String version;
        }

        public RQuery(String str, RQueryData rQueryData) {
            this.action = str;
            this.data = rQueryData;
        }
    }

    public static class RDownload {
        String action;
        RDownloadData data;

        public static class RDownloadData {
            public int apn;
            public int background;
            public long duration;
            public String status;
            public String time;
            public int type;
            public String version;
        }

        public RDownload(String str, RDownloadData rDownloadData) {
            this.action = str;
            this.data = rDownloadData;
        }
    }

    public static class RInstall {
        String action;
        RInstallData data;

        public static class RInstallData {
            public int forced;
            public String newVersion;
            public String oldVersion;
            public String status;
            public String time;
            public int type;
        }

        public RInstall(String str, RInstallData rInstallData) {
            this.action = str;
            this.data = rInstallData;
        }
    }

    public static class RInstallResult {
        String action;
        RInstallResultData data;

        public static class RInstallResultData {
            public String errCode;
            public String newVersion;
            public String oldVersion;
            public String reason;
            public int status;
            public String time;
            public int type;
        }

        public RInstallResult(String str, RInstallResultData rInstallResultData) {
            this.action = str;
            this.data = rInstallResultData;
        }
    }
}
