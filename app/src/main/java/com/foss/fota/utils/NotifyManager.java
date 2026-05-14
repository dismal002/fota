package com.foss.fota.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import com.foss.fota.BatteryService;
import com.foss.fota.GoogleOtaClient;
import com.foss.fota.MyApplication;
import com.foss.fota.R;
import com.foss.fota.update.query.QueryInfo;

/* JADX INFO: compiled from: NotifyManager.java */
/* JADX INFO: loaded from: classes.dex */
public class NotifyManager {
    private static NotifyManager instance = null;
    private NotificationManager notificationManager;

    private NotifyManager(Context context) {
        this.notificationManager = (NotificationManager) context.getSystemService("notification");
    }

    public static NotifyManager a(Context context) {
        return getInstance(context);
    }

    public void a(Context context, int id) {
        clearNotification(context, id);
    }

    public static NotifyManager getInstance(Context context) {
        if (instance == null) {
            synchronized (NotifyManager.class) {
                if (instance == null) {
                    instance = new NotifyManager(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    public void showNewVersion(Context context, boolean isResident) {
        Intent intent = new Intent(context, GoogleOtaClient.class);
        intent.addFlags(67108864);
        notify(context, R.string.appbar_scrolling_view_behavior, context.getString(R.string.notification_content_newversion), PendingIntent.getActivity(context, 1, intent, 268435456), isResident);
    }

    public void showDownloadProgress(Context context, String title, String content) {
        if (isProgressNotifyEnabled(context)) {
            Intent intent = new Intent(context, GoogleOtaClient.class);
            intent.addFlags(67108864);
            notify(context, R.string.appbar_scrolling_view_behavior, content, PendingIntent.getActivity(context, 2, intent, 268435456), false);
        }
    }

    public void showDownloadPaused(Context context, boolean isResident) {
        if (isProgressNotifyEnabled(context)) {
            Intent intent = new Intent(context, GoogleOtaClient.class);
            intent.addFlags(67108864);
            notify(context, R.string.appbar_scrolling_view_behavior, context.getString(R.string.ota_notify_download_pause_content), PendingIntent.getActivity(context, 3, intent, 268435456), isResident);
        }
    }

    public void showDownloadComplete(Context context, boolean isResident) {
        if (com.foss.fota.update.install.Install.isAbUpdateSupported() && !com.foss.fota.update.install.Install.isBatteryEnough(MyApplication.getInstance(), 30)) {
            context.startService(new Intent(context, BatteryService.class));
            return;
        }
        Intent intent = new Intent(context, GoogleOtaClient.class);
        intent.addFlags(67108864);
        notify(context, R.string.appbar_scrolling_view_behavior, context.getString(R.string.download_completed_text), PendingIntent.getActivity(context, 4, intent, 268435456), isResident);
    }

    private void notify(Context context, int id, String content, PendingIntent pendingIntent, boolean isResident) {
        String title = context.getText(R.string.app_name).toString();
        Notification.Builder builder = new Notification.Builder(context)
                .setSmallIcon(R.mipmap.status_bar_icon)
                .setLargeIcon(BitmapFactory.decodeResource(MyApplication.getInstance().getResources(), R.mipmap.icon_update))
                .setTicker(title)
                .setContentTitle(title)
                .setContentText(content)
                .setContentIntent(pendingIntent);
        
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel("channel_fota", title, 2);
            channel.enableVibration(false);
            channel.enableLights(false);
            this.notificationManager.createNotificationChannel(channel);
            builder.setChannelId("channel_fota");
        }
        
        Notification notification = builder.build();
        if (isResident) {
            notification.flags |= 162; // FLAG_ONGOING_EVENT | FLAG_NO_CLEAR
        } else {
            notification.flags |= 16;  // FLAG_AUTO_CANCEL
        }
        
        this.notificationManager.notify(id, notification);
    }

    public void clearNotification(Context context, int id) {
        if (Build.VERSION.SDK_INT >= 26) {
            this.notificationManager.deleteNotificationChannel(String.valueOf(id));
        }
        this.notificationManager.cancel(id);
    }

    public void cancelNotification(Context context, boolean force) {
        clearNotification(context, R.string.appbar_scrolling_view_behavior);
    }

    public boolean isProgressNotifyEnabled(Context context) {
        int noticeType = ((Integer) QueryInfo.getInstance(context).getPolicy("query_notice_type", Integer.class)).intValue();
        Trace.d("isShowProgressNotify", "isShowProgressNotify = " + noticeType);
        return noticeType == 0;
    }
}
