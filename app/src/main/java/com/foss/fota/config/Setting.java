package com.foss.fota.config;

import com.foss.fota.utils.DeviceUtil;

/* JADX INFO: compiled from: Setting.java */
/* JADX INFO: loaded from: classes.dex */
public final class Setting {
    public static int getUpdateInterval() {
        String strS = DeviceUtil.getInstance().getUpdateCycle();
        if ("0".equals(strS) || "1".equals(strS)) {
            return 1440;
        }
        if ("3".equals(strS)) {
            return 4320;
        }
        return "7".equals(strS) ? 10080 : 1440;
    }
}
