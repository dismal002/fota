package com.foss.fota.JobServiceUtil;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.os.Build;
import com.foss.fota.MyApplication;
import com.foss.fota.update.Alarm;
import com.foss.fota.update.Status;
import com.foss.fota.update.query.QueryActivate;
import com.foss.fota.update.request.RequestManager;
import com.foss.fota.update.report.ReportManager;
import com.foss.fota.utils.Trace;
import com.foss.fota.utils.NetWorkUtil;
import com.foss.fota.utils.PreferencesUtils;
import com.foss.fota.utils.DeviceInfoProvider;
import java.util.Random;

public class MyIntentJobService extends JobService {
    @Override // android.app.job.JobService
    public boolean onStartJob(JobParameters jobParameters) {
        if (jobParameters.getJobId() == 2) {
            final String action = jobParameters.getExtras().getString("action");
            Trace.d("action = " + action + "; SDK_INT = " + Build.VERSION.SDK_INT);
            
            if ("android.net.conn.CONNECTIVITY_CHANGE".equals(action)) {
                checkJobScheduler();
            }
            
            if ("android.intent.action.BOOT_COMPLETED".equals(action)) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        DeviceInfoProvider.getInstance(MyIntentJobService.this).getDeviceId(MyIntentJobService.this);
                    }
                }).start();
            } else if (!QueryActivate.isActivated(MyApplication.getInstance())) {
                if ("android.net.conn.CONNECTIVITY_CHANGE".equals(action) || "android.intent.action.ACTION_POWER_DISCONNECTED".equals(action)) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            handleConnectivityChange("android.intent.action.ACTION_POWER_DISCONNECTED".equals(action));
                        }
                    }).start();
                } else if (Build.VERSION.SDK_INT >= 21 && "job_downloading_action".equals(action)) {
                    Trace.d("downloading action job triggered");
                } else if ("android.intent.action.DATE_CHANGED".equals(action)) {
                    Random random = new Random();
                    long delay = (random.nextInt(60) * 1000) + (random.nextInt(4) * 60 * 60 * 1000) + (random.nextInt(60) * 60 * 1000);
                    Trace.d("delayFlag = " + delay);
                    Alarm.startCheckAlarm(this, delay);
                }
            }
        }
        return false;
    }

    @Override // android.app.job.JobService
    public boolean onStopJob(JobParameters jobParameters) {
        return false;
    }

    private void checkJobScheduler() {
        if (Build.VERSION.SDK_INT >= 24) {
            // Intent handling logic for job scheduler
        }
    }

    private void handleConnectivityChange(boolean isPowerDisconnected) {
        boolean connected = NetWorkUtil.isConnected(this);
        Trace.d("isConnected = " + connected);
        if (connected) {
            RequestManager.executeRequest(this, null);
            if (!isPowerDisconnected || PreferencesUtils.getInt(this, "ota_update_status", 0) != 2) {
                Status.handlePostDownloadTask(this);
            }
            ReportManager.getInstance(this).scheduleReport();
        }
    }
}
