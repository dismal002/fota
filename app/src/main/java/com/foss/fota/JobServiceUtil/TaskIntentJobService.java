package com.foss.fota.JobServiceUtil;

import android.app.job.JobParameters;
import android.app.job.JobService;
import androidx.core.app.NotificationCompat;
import com.foss.fota.update.Notice;
import com.foss.fota.update.Status;
import com.foss.fota.update.download.DownVersion;
import com.foss.fota.update.query.QueryVersion;
import com.foss.fota.utils.Trace;

/* JADX INFO: loaded from: classes.dex */
public class TaskIntentJobService extends JobService {
    @Override // android.app.job.JobService
    public boolean onStartJob(JobParameters jobParameters) {
        if (jobParameters.getJobId() == 3) {
            final int i = jobParameters.getExtras().getInt("task_id");
            final int i2 = jobParameters.getExtras().getInt(NotificationCompat.CATEGORY_STATUS);
            final String string = jobParameters.getExtras().getString("value");
            new Thread(new Runnable() { // from class: com.foss.fota.JobServiceUtil.TaskIntentJobService.1
                @Override // java.lang.Runnable
                public void run() {
                    TaskIntentJobService.this.a(i, i2, string);
                }
            }).start();
            return false;
        }
        return false;
    }

    @Override // android.app.job.JobService
    public boolean onStopJob(JobParameters jobParameters) {
        return false;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void a(int i, int i2, String str) {
        Trace.d("task_id = " + i + "; status = ; value = " + str);
        switch (i) {
            case 1:
                QueryVersion.getInstance(this).checkSchedule();
                break;
            case 2:
                Notice.showUpdateNotice(this);
                break;
            case 4:
                Status.handlePostDownloadTask(this);
                break;
            case 6:
                Notice.showInstallNotice(this);
                break;
            case 7:
                Notice.startClient(this, i2);
                break;
            case 9:
                // No-op: drawer control is activity-only.
                break;
            case 10:
                QueryVersion.getInstance(this).startNormalQuery(1);
                break;
            case 11:
                DownVersion.getInstance(this).stopDownload();
                break;
            case 12:
                Status.cancelUpdate(this);
                break;
        }
    }
}
