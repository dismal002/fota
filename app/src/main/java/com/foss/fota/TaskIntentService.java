package com.foss.fota;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import com.foss.fota.update.Status;
import com.foss.fota.update.download.DownVersion;
import com.foss.fota.update.Notice;
import com.foss.fota.update.query.QueryVersion;
import com.foss.fota.utils.Trace;

public class TaskIntentService extends BaseService {
    public TaskIntentService() {
        super("AlarmIntentService");
    }

    public static void a(Context context, int i, int i2, String str) {
        Intent intent = new Intent(context, (Class<?>) TaskIntentService.class);
        intent.putExtra("task", i);
        intent.putExtra(NotificationCompat.CATEGORY_STATUS, i2);
        intent.putExtra("value", str);
        if (Build.VERSION.SDK_INT >= 26) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }

    // Compatibility alias
    public static void start(Context context, int task, int status, String value) {
        a(context, task, status, value);
    }

    @Override // com.foss.fota.BaseService, android.app.IntentService
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            try {
                a(intent.getIntExtra("task", 0), intent.getIntExtra(NotificationCompat.CATEGORY_STATUS, 0), intent.getStringExtra("value"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void a(int i, int i2, String str) {
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
