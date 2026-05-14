package com.foss.fota.JobServiceUtil;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import com.foss.fota.R;
import android.os.Build;
import android.os.IBinder;
import com.foss.fota.GoogleOtaClient;
import com.foss.fota.MyApplication;
import com.foss.fota.update.query.QueryInfo;
import com.foss.fota.utils.Trace;

/* JADX INFO: loaded from: classes.dex */
public class BaseBatteryService extends Service {
    private String a;

    public BaseBatteryService(String str) {
        this.a = str;
    }

    @Override // android.app.Service
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= 26) {
            b();
            startForeground(1, a());
        }
        Trace.d(this.a);
    }

    @Override // android.app.Service
    public void onDestroy() {
        super.onDestroy();
        Trace.d(this.a);
    }

    @Override // android.app.Service
    public IBinder onBind(Intent intent) {
        return null;
    }

    private Notification a() {
        Notification.Builder builder = new Notification.Builder(this, String.valueOf(1));
        Intent intent = new Intent(MyApplication.getInstance(), (Class<?>) GoogleOtaClient.class);
        intent.addFlags(67108864);
        builder.setContentTitle(MyApplication.getInstance().getText(R.string.app_name)).setContentText(MyApplication.getInstance().getString(R.string.download_completed_text)).setTicker(MyApplication.getInstance().getText(R.string.app_name)).setSmallIcon(R.mipmap.status_bar_icon).setLargeIcon(BitmapFactory.decodeResource(MyApplication.getInstance().getResources(), R.mipmap.icon_update)).setContentIntent(PendingIntent.getActivity(MyApplication.getInstance(), 4, intent, 268435456)).setContentTitle(MyApplication.getInstance().getText(R.string.app_name));
        Notification notificationBuild = builder.build();
        if (((Boolean) QueryInfo.getInstance(MyApplication.getInstance()).getPolicy("install_notice_resident", Boolean.class)).booleanValue()) {
            notificationBuild.flags = 162;
        } else {
            notificationBuild.flags = 144;
        }
        return notificationBuild;
    }

    private void b() {
        NotificationManager notificationManager = (NotificationManager) getSystemService("notification");
        NotificationChannel notificationChannel = new NotificationChannel(String.valueOf(1), this.a, 2);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }
}
