package com.foss.fota;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import com.foss.fota.utils.Trace;

public class BaseService extends IntentService {
    private String a;

    public BaseService(String str) {
        super(str);
        this.a = str;
    }

    @Override // android.app.IntentService, android.app.Service
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= 26) {
            b();
            startForeground(1, a());
        }
        Trace.d(this.a);
    }

    @Override // android.app.IntentService, android.app.Service
    public void onDestroy() {
        super.onDestroy();
        Trace.d(this.a);
    }

    @Override // android.app.IntentService
    protected void onHandleIntent(Intent intent) {
    }

    private Notification a() {
        Notification.Builder builder = new Notification.Builder(this, String.valueOf(1));
        builder.setTicker("").setContentTitle("").setSmallIcon(R.mipmap.ic_launcher);
        return builder.build();
    }

    private void b() {
        NotificationManager notificationManager = (NotificationManager) getSystemService("notification");
        NotificationChannel notificationChannel = new NotificationChannel(String.valueOf(1), this.a, 2);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }
}
