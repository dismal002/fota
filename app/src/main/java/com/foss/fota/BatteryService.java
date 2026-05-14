package com.foss.fota;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.PowerManager;
import com.foss.fota.JobServiceUtil.BaseBatteryService;
import com.foss.fota.utils.Trace;
import com.foss.fota.utils.NotifyManager;
import com.foss.fota.R;
import com.foss.fota.utils.PreferencesUtils;

public class BatteryService extends BaseBatteryService {
    BroadcastReceiver a;
    private boolean b;

    public BatteryService() {
        super("BatteryService");
        this.a = new BroadcastReceiver() { // from class: com.foss.fota.BatteryService.1
            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                int iB = PreferencesUtils.b(context, "ota_update_status", 0);
                Trace.d("BatteryService", "status=" + iB + ",screen=" + ((PowerManager) context.getSystemService("power")).isScreenOn());
                if (iB == 4 && intent.getAction().equals("android.intent.action.BATTERY_CHANGED")) {
                    int intExtra = intent.getIntExtra("level", 0);
                    Trace.d("BatteryService", "level=" + intExtra);
                    if (intExtra > 29) {
                        com.foss.fota.update.report.ReportData.reportQuery(context, "auto");
                        com.foss.fota.update.install.Install.b(context.getApplicationContext());
                    }
                }
            }
        };
        this.b = false;
    }

    @Override // com.foss.fota.JobServiceUtil.BaseBatteryService, android.app.Service
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override // android.app.Service
    public int onStartCommand(Intent intent, int i, int i2) {
        if (intent == null || this.b) {
            return 2;
        }
        NotifyManager.a(MyApplication.getInstance()).a(getApplicationContext(), R.string.appbar_scrolling_view_behavior);
        try {
            registerReceiver(this.a, new IntentFilter("android.intent.action.BATTERY_CHANGED"));
        } catch (Exception e) {
        }
        this.b = true;
        Trace.d("BatteryService", "onStartCommand() registerReceiver");
        return super.onStartCommand(intent, i, i2);
    }

    @Override // com.foss.fota.JobServiceUtil.BaseBatteryService, android.app.Service
    public void onDestroy() {
        Trace.d("BatteryService,onDestroy");
        if (this.a != null) {
            unregisterReceiver(this.a);
        }
        super.onDestroy();
    }
}
