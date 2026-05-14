package com.foss.fota;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.Build;
import com.foss.fota.update.install.InstallResult;
import com.foss.fota.update.Reboot;
import com.foss.fota.update.download.DownVersion;
import com.foss.fota.utils.Trace;
import com.foss.fota.utils.NetWorkUtil;
import com.foss.fota.utils.PreferencesUtils;
import java.util.Random;

public class MyIntentService extends BaseService {
    public MyIntentService() {
        super("MyIntentService");
    }

    @Override // com.foss.fota.BaseService, android.app.IntentService
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            String action = intent.getAction();
            Trace.d("action = " + action + "; SDK_INT = " + Build.VERSION.SDK_INT);
            if ("android.net.conn.CONNECTIVITY_CHANGE".equals(action)) {
                a();
            }
            if ("android.intent.action.BOOT_COMPLETED".equals(action)) {
                com.foss.fota.utils.DeviceInfoProvider.getInstance(this);
                return;
            }
            if (!com.foss.fota.utils.DeviceUtil.getInstance().isGoogleOta()) {
                if ("android.net.conn.CONNECTIVITY_CHANGE".equals(action) || "android.intent.action.ACTION_POWER_DISCONNECTED".equals(action)) {
                    a("android.intent.action.ACTION_POWER_DISCONNECTED".equals(action));
                    return;
                }
                if ("android.intent.action.DATE_CHANGED".equals(action)) {
                    Random random = new Random();
                    long jNextInt = (random.nextInt(60) * 1000) + (random.nextInt(4) * 60 * 60 * 1000) + (random.nextInt(60) * 60 * 1000);
                    Trace.d("delayFlag = " + jNextInt);
                    com.foss.fota.update.Alarm.e(this, jNextInt);
                }
            }
        }
    }

    private void a() {
        if (Build.VERSION.SDK_INT >= 24) {
            for (ResolveInfo resolveInfo : getPackageManager().queryBroadcastReceivers(new Intent("com.foss.fota.job_scheduler"), 0)) {
                if (resolveInfo.activityInfo.packageName.equals(getPackageName())) {
                    Intent intent = new Intent();
                    intent.setAction("com.foss.fota.job_scheduler");
                    intent.setClassName(getPackageName(), resolveInfo.activityInfo.name);
                    sendBroadcast(intent);
                }
            }
        }
    }

    private void a(boolean z) {
        boolean zA = NetWorkUtil.a(this);
        Trace.d("isConnected = " + zA);
        if (zA) {
            com.foss.fota.update.request.RequestManager.getInstance(this).query();
            if (!z || PreferencesUtils.getInt(this, "ota_update_status", 0) != 2) {
                // No-op: legacy helper removed during migration.
            }
            com.foss.fota.update.query.QueryVersion.getInstance(this).checkSchedule();
        }
    }
}
