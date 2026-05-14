package com.foss.fota.update;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.SystemClock;
import androidx.core.app.NotificationCompat;
import androidx.core.view.PointerIconCompat;
import com.foss.fota.MyReceiver;
import com.foss.fota.utils.Trace;
import com.foss.fota.utils.PreferencesUtils;
import com.foss.fota.utils.DeviceUtil;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class Alarm {
    private static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");

    private static void setAlarm(Context context, Class<?> cls, String action, int task, long time, int requestCode) {
        Intent intent = new Intent(context, cls);
        if (action != null) {
            intent.setAction(action);
        }
        if (task != Integer.MAX_VALUE) {
            intent.putExtra("task", task);
        }
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= 23) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        PendingIntent broadcast = PendingIntent.getBroadcast(context, requestCode, intent, flags);
        if (alarmManager == null) {
            Trace.d("alarmMgr == null");
            return;
        }
        alarmManager.cancel(broadcast);
        if (Build.VERSION.SDK_INT >= 19) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, time, broadcast);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, time, broadcast);
        }
    }

    public static void startCheckAlarm(Context context) {
        long time = (System.currentTimeMillis() + DeviceUtil.getInstance().getActivateTime())
                - SystemClock.elapsedRealtime();
        setAlarm(context, MyReceiver.class, com.foss.fota.config.Const.ALARM_ACTION, 2, time,
                PointerIconCompat.TYPE_ALIAS);
        Trace.d("alarm time = " + dateFormat.format(time));
    }

    public static void startCheckAlarm(Context context, long delay) {
        long time = System.currentTimeMillis() + delay;
        setAlarm(context, MyReceiver.class, com.foss.fota.config.Const.ALARM_ACTION, 1, time,
                PointerIconCompat.TYPE_GRAB);
        Trace.d("alarm time = " + dateFormat.format(time));
    }

    public static void setForceUpdateAlarm(Context context, long time) {
        setAlarm(context, MyReceiver.class, com.foss.fota.config.Const.ALARM_ACTION, 4, time, 1030);
        Trace.d("alarm time = " + dateFormat.format(time));
    }

    public static void setInstallDelayAlarm(Context context, long time) {
        setAlarm(context, MyReceiver.class, com.foss.fota.config.Const.ALARM_ACTION, 6, time, 1040);
        Trace.d("alarm time = " + dateFormat.format(time));
    }

    public static void setForceRebootAlarm(Context context, long time) {
        setAlarm(context, MyReceiver.class, com.foss.fota.config.Const.ALARM_ACTION, 12, time, 1060);
        Trace.d("reboot_force  alarm time = " + dateFormat.format(time));
    }

    public static void startRetryAlarm(Context context, long delay) {
        long time = System.currentTimeMillis() + delay;
        setAlarm(context, MyReceiver.class, com.foss.fota.config.Const.ALARM_ACTION, 1, time, 1080);
        Trace.d("alarm time = " + dateFormat.format(time));
    }

    public static long getCheckInterval(Context context) {
        long freq = PreferencesUtils.getLong(context, "check_freq", 2940L);
        if (freq == 2940) {
            freq = PreferencesUtils.getLong(context, "check_local_freq", 2940L);
        }
        return freq * 1000 * 60;
    }

    public static void cancelUpdate(Context context) {
        startCheckAlarm(context, getCheckInterval(context));
    }

    // Compatibility aliases
    public static void a(Context context) {
        startCheckAlarm(context);
    }

    public static void a(Context context, long delay) {
        startCheckAlarm(context, delay);
    }

    public static void b(Context context, long time) {
        setForceUpdateAlarm(context, time);
    }

    public static void c(Context context, long time) {
        setInstallDelayAlarm(context, time);
    }

    public static void d(Context context, long time) {
        setForceRebootAlarm(context, time);
    }

    public static void e(Context context, long delay) {
        startRetryAlarm(context, delay);
    }

    public static long b(Context context) {
        return getCheckInterval(context);
    }

    public static void c(Context context) {
        cancelUpdate(context);
    }

    public static void setInstallAlarm(Context context) {
        long time = System.currentTimeMillis() + 60000; // default 1 minute delay
        setAlarm(context, MyReceiver.class, com.foss.fota.config.Const.ALARM_ACTION, 5, time, 1035);
        Trace.d("install alarm time = " + dateFormat.format(time));
    }

    public static void startInstallAlarm(Context context) {
        setInstallAlarm(context);
    }
}
