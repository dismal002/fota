package com.foss.fota.utils;

import android.annotation.TargetApi;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;
import android.os.PersistableBundle;
import androidx.core.app.NotificationCompat;
import android.text.TextUtils;

public class IntentUtil {
    @TargetApi(21)
    public static boolean a(Context context, int i, Class<?> cls, int i2, int i3, String str) {
        if (Build.VERSION.SDK_INT < 21) {
            return false;
        }
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService("jobscheduler");
        ComponentName componentName = new ComponentName(context, cls);
        PersistableBundle persistableBundle = new PersistableBundle();
        persistableBundle.putInt("task_id", i2);
        persistableBundle.putInt(NotificationCompat.CATEGORY_STATUS, i3);
        persistableBundle.putString("value", str);
        jobScheduler.schedule(new JobInfo.Builder(i, componentName).setMinimumLatency(0L).setOverrideDeadline(0L).setExtras(persistableBundle).build());
        Trace.d("IntentUtil:", "[set24JobSchedulerByInt] set job id=" + i);
        return true;
    }

    @TargetApi(21)
    public static boolean a(Context context, int i, Class<?> cls, String str) {
        JobInfo jobInfoBuild;
        if (Build.VERSION.SDK_INT >= 21) {
            JobScheduler jobScheduler = (JobScheduler) context.getSystemService("jobscheduler");
            ComponentName componentName = new ComponentName(context, cls);
            if (TextUtils.isEmpty(str)) {
                jobInfoBuild = new JobInfo.Builder(i, componentName).setMinimumLatency(0L).setOverrideDeadline(0L).setPersisted(true).build();
            } else {
                PersistableBundle persistableBundle = new PersistableBundle();
                persistableBundle.putString("action", str);
                jobInfoBuild = new JobInfo.Builder(i, componentName).setMinimumLatency(0L).setOverrideDeadline(0L).setPersisted(true).setExtras(persistableBundle).build();
            }
            jobScheduler.schedule(jobInfoBuild);
            Trace.d("IntentUtil:", "[set24JobScheduler] set job id=" + i);
            return true;
        }
        return false;
    }
}
