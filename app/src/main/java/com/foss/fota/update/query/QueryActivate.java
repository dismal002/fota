package com.foss.fota.update.query;

import android.content.Context;
import android.os.SystemClock;
import android.text.TextUtils;
import com.foss.fota.utils.Trace;
import com.foss.fota.utils.FileUtil;
import com.foss.fota.utils.PreferencesUtils;
import com.foss.fota.utils.DeviceUtil;

/* JADX INFO: compiled from: QueryActivate.java */
/* JADX INFO: loaded from: classes.dex */
public class QueryActivate {
    public static boolean isActivated(Context context) {
        long t = DeviceUtil.getInstance().getActivateTime();
        if (SystemClock.elapsedRealtime() >= t || PreferencesUtils.getLong(context, "activate_total_time", 0L) >= t) {
            return true;
        }
        if (FileUtil.isSdcardMounted()) {
            return checkActivationFile(context);
        }
        return false;
    }

    public static void updateActivation(Context context) {
        Trace.d("enter");
        long t = DeviceUtil.getInstance().getActivateTime();
        if (SystemClock.elapsedRealtime() < t) {
            QueryVersion.getInstance(context).onQueryScheduleTask();
            return;
        }
        PreferencesUtils.putLong(context, "activate_total_time", t);
        if (FileUtil.isSdcardMounted()) {
            createActivationFile(context);
        }
    }

    public static void checkActivation(Context context) {
        if (isActivated(context)) {
            Trace.d("ota is activated");
        } else {
            QueryVersion.getInstance(context).onQueryScheduleTask();
        }
    }

    private static boolean checkActivationFile(Context context) {
        // Mocking the complex check for now, as h.i and x.clearUpdateData are likely obfuscated remnants
        return false;
    }

    private static void createActivationFile(Context context) {
        try {
            // Mocking the complex write for now
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }

    // Compatibility alias
    public static boolean a(Context context) {
        return isActivated(context);
    }
}
