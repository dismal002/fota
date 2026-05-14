package com.foss.fota;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.Build;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.foss.fota.JobServiceUtil.MyIntentJobService;
import com.foss.fota.update.query.QueryInfo;
import com.foss.fota.utils.Trace;
import com.foss.fota.utils.DeviceUtil;
import com.foss.fota.utils.IntentUtil;
import com.foss.fota.utils.PreferencesUtils;

/* JADX INFO: loaded from: classes.dex */
@TargetApi(21)
public class MyJobService extends JobService {
    public static final long MINUTE_UNIT = 60000;
    public static final long HOUR_UNIT = 60 * MINUTE_UNIT;
    public static final long DAY_UNIT = 24 * HOUR_UNIT;
    public static final long INTERVAL_UNIT = MINUTE_UNIT;

    private static long connectivityInterval = 60 * MINUTE_UNIT;
    private static long connectivityDeadline = 1440 * MINUTE_UNIT;
    private static long downloadInterval = 60 * MINUTE_UNIT;
    private static long downloadDeadline = 1440 * MINUTE_UNIT;

    public static void updateJobIntervals(Context context) {
        try {
            String[] connectivityTimes = PreferencesUtils.getString(context, "job_schedule_time", "60#1440").split("#");
            connectivityInterval = Long.parseLong(connectivityTimes[0]) * MINUTE_UNIT;
            connectivityDeadline = Long.parseLong(connectivityTimes[1]) * MINUTE_UNIT;

            String[] downloadTimes = PreferencesUtils.getString(context, "job_schedule_downloading_time", "60#1440")
                    .split("#");
            downloadInterval = Long.parseLong(downloadTimes[0]) * MINUTE_UNIT;
            downloadDeadline = Long.parseLong(downloadTimes[1]) * MINUTE_UNIT;
        } catch (Exception e) {
            connectivityInterval = 60 * MINUTE_UNIT;
            connectivityDeadline = 1440 * MINUTE_UNIT;
            downloadInterval = 60 * MINUTE_UNIT;
            downloadDeadline = 1440 * MINUTE_UNIT;
        }
    }

    public static boolean scheduleJob(Context context, int jobId, long latency, long deadline) {
        if (Build.VERSION.SDK_INT < 24) {
            return false;
        }
        JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        if (scheduler == null)
            return false;

        JobInfo.Builder builder = new JobInfo.Builder(jobId, new ComponentName(context, MyJobService.class))
                .setMinimumLatency(latency)
                .setOverrideDeadline(deadline)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPersisted(true);

        scheduler.schedule(builder.build());
        Trace.d("MyJobService", "Scheduled job id=" + jobId + " latency=" + latency + " deadline=" + deadline);
        return true;
    }

    // Compatibility alias used by ParserVersion.
    public static void schedule(Context context) {
        updateJobIntervals(context);
        // Best-effort: schedule the connectivity job which drives periodic checks.
        scheduleJob(context, 1000, connectivityInterval, connectivityDeadline);
    }

    public static void schedule(Context context, int jobId, long latency, long deadline) {
        scheduleJob(context, jobId, latency, deadline);
    }

    @Override // android.app.Service
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= 26) {
            setupNotificationChannel();
            startForeground(1, createForegroundNotification());
        }
        Trace.d("MyJobService onCreate");
    }

    private Notification createForegroundNotification() {
        Notification.Builder builder = new Notification.Builder(this, "channel_job_service")
                .setTicker("")
                .setContentTitle("")
                .setSmallIcon(R.mipmap.ic_launcher);
        return builder.build();
    }

    private void setupNotificationChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel channel = new NotificationChannel("channel_job_service", "MyJobService",
                    NotificationManager.IMPORTANCE_LOW);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    @Override // android.app.job.JobService
    public boolean onStartJob(JobParameters params) {
        int jobId = params.getJobId();
        Trace.d("MyJobService", "onStartJob, id=" + jobId);

        updateJobIntervals(this);

        if (jobId == 1000) {
            handleConnectivityJob();
            scheduleJob(this, 1000, connectivityInterval, connectivityDeadline);
        } else if (jobId == 1006) {
            handleDownloadJob();
        }

        return false;
    }

    private void handleConnectivityJob() {
        if (Build.VERSION.SDK_INT >= 26) {
            IntentUtil.a(this, 2, MyIntentJobService.class, "android.net.conn.CONNECTIVITY_CHANGE");
        } else {
            Intent intent = new Intent(this, MyIntentService.class);
            intent.setAction("android.net.conn.CONNECTIVITY_CHANGE");
            startService(intent);
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent("com.foss.fota.job_scheduler"));
    }

    private void handleDownloadJob() {
        int status = PreferencesUtils.getInt(this, "ota_update_status", 0);
        if (status == 2 || status == 3) {
            if (isAutoDownloadEnabled()) {
                if (Build.VERSION.SDK_INT >= 26) {
                    IntentUtil.a(this, 2, MyIntentJobService.class, "job_downloading_action");
                } else {
                    Intent intent = new Intent(this, MyIntentService.class);
                    intent.setAction("job_downloading_action");
                    startService(intent);
                }
                scheduleJob(this, 1006, downloadInterval, downloadDeadline);
            }
        }
    }

    private boolean isAutoDownloadEnabled() {
        try {
            int autoDownload = ((Integer) QueryInfo.getInstance(this).getPolicy("download_auto", Integer.class))
                    .intValue();
            boolean wifiOnly = PreferencesUtils.getBoolean(this, "download_wifi_auto",
                    DeviceUtil.getInstance().isWifiAutoEnabled());
            return autoDownload != 2 && wifiOnly;
        } catch (Exception e) {
            return false;
        }
    }

    @Override // android.app.job.JobService
    public boolean onStopJob(JobParameters params) {
        jobFinished(params, false);
        return false;
    }
}
