package com.foss.fota.update;

import android.app.ActivityManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Icon;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.content.pm.ShortcutManagerCompat;
import com.foss.fota.FotaPopWindow;
import com.foss.fota.GoogleOtaClient;
import com.foss.fota.MyApplication;
import com.foss.fota.MyReceiver;
import com.foss.fota.ShortCutActivity;
import com.foss.fota.utils.Trace;
import com.foss.fota.utils.NotifyManager;
import com.foss.fota.utils.PreferencesUtils;
import java.util.List;

/* JADX INFO: compiled from: Notice.java */
/* JADX INFO: loaded from: classes.dex */
public class Notice {
    public static void showUpdateNotice(Context context) {
        int noticeType = ((Integer) com.foss.fota.update.query.QueryInfo.getInstance(context).getPolicy("query_notice_type", Integer.class)).intValue();
        boolean isResident = ((Boolean) com.foss.fota.update.query.QueryInfo.getInstance(context).getPolicy("query_notice_resident", Boolean.class)).booleanValue();
        int status = PreferencesUtils.b(context, "ota_update_status");
        Trace.d("notice_type = " + noticeType + "; notice_resident = " + isResident);
        if (noticeType == 0) {
            if (status == 4) {
                NotifyManager.getInstance(context).showDownloadComplete(context, isResident);
            } else {
                NotifyManager.getInstance(context).showNewVersion(context, isResident);
            }
        } else if (noticeType == 1) {
            updateShortcut(context, 2);
        } else if (noticeType == 2 && !com.foss.fota.utils.ActivityStackUtil.getType()) {
            Intent intent = new Intent(context, (Class<?>) FotaPopWindow.class);
            intent.addFlags(268435456);
            intent.putExtra(NotificationCompat.CATEGORY_STATUS, 1);
            context.startActivity(intent);
        }
    }

    public static void showInstallNotice(Context context) {
        if (PreferencesUtils.b(context, "ota_update_status") == 4) {
            int noticeType = ((Integer) com.foss.fota.update.query.QueryInfo.getInstance(context).getPolicy("install_notice_type", Integer.class)).intValue();
            boolean isResident = ((Boolean) com.foss.fota.update.query.QueryInfo.getInstance(context).getPolicy("install_notice_resident", Boolean.class)).booleanValue();
            Trace.d("Notice", "ota download notice : notice_type = " + noticeType + "||notice_resident = " + isResident);
            if (noticeType == 0) {
                NotifyManager.getInstance(context).showDownloadComplete(context, isResident);
            } else if (noticeType == 1) {
                updateShortcut(context, 2);
            } else if (noticeType == 2 && !com.foss.fota.utils.ActivityStackUtil.getType()) {
                Intent intent = new Intent(context, (Class<?>) FotaPopWindow.class);
                intent.addFlags(268435456);
                intent.putExtra(NotificationCompat.CATEGORY_STATUS, 4);
                context.startActivity(intent);
            }
        }
    }

    public static void cancelUpdate(Context context) {
        NotifyManager.getInstance(context).cancelNotification(context, false);
    }

    public static void clearUpdateNotification(Context context) {
        NotifyManager.getInstance(context).clearNotification(context, com.foss.fota.R.string.appbar_scrolling_view_behavior);
    }

    public static void startClient(Context context, int flag) {
        if (flag == 1) {
            Intent intent = new Intent(context, (Class<?>) GoogleOtaClient.class);
            intent.addFlags(335544320);
            context.startActivity(intent);
        }
    }

    public static void uninstallShortcut(Context context) {
        Intent intent = new Intent("com.android.launcher.action.UNINSTALL_SHORTCUT");
        intent.putExtra("android.intent.extra.shortcut.NAME", context.getString(com.foss.fota.R.string.app_name));
        intent.putExtra("android.intent.extra.shortcut.INTENT", new Intent("android.intent.action.MAIN").setComponent(new ComponentName(context.getPackageName(), "com.foss.fota.GoogleOtaClient")));
        context.sendBroadcast(intent);
    }

    public static void installShortcut(Context context) {
        Intent intent = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
        intent.putExtra("android.intent.extra.shortcut.NAME", context.getString(com.foss.fota.R.string.app_name));
        intent.putExtra("duplicate", false);
        intent.putExtra("android.intent.extra.shortcut.ICON_RESOURCE", Intent.ShortcutIconResource.fromContext(context, com.foss.fota.R.mipmap.newversion_shortcut));
        Intent component = new Intent("android.intent.action.MAIN").setComponent(new ComponentName(context.getPackageName(), "com.foss.fota.GoogleOtaClient"));
        component.putExtra("isShortcut", true);
        intent.putExtra("android.intent.extra.shortcut.INTENT", component);
        if (Build.VERSION.SDK_INT >= 26 && ShortcutManagerCompat.isRequestPinShortcutSupported(context)) {
            component.setAction("android.intent.action.VIEW");
            ShortcutManager shortcutManager = (ShortcutManager) context.getSystemService(ShortcutManager.class);
            PendingIntent broadcast = PendingIntent.getBroadcast(context, 0, new Intent(context, (Class<?>) MyReceiver.class), 134217728);
            if (shortcutManager != null && shortcutManager.getPinnedShortcuts().size() == 0) {
                if (isAppInForeground()) {
                    shortcutManager.requestPinShortcut(createShortcutInfo(context, component), broadcast.getIntentSender());
                } else {
                    Intent intent2 = new Intent(context, (Class<?>) ShortCutActivity.class);
                    intent2.addFlags(268435456);
                    context.startActivity(intent2);
                }
            }
        } else {
            intent.putExtra("android.intent.extra.shortcut.ICON_RESOURCE", Intent.ShortcutIconResource.fromContext(context, com.foss.fota.R.mipmap.newversion_shortcut));
            context.sendBroadcast(intent);
        }
    }

    private static boolean isAppInForeground() {
        try {
            List<ActivityManager.RunningTaskInfo> runningTasks = ((ActivityManager) MyApplication.getInstance().getSystemService("activity")).getRunningTasks(1);
            if (runningTasks != null && !runningTasks.isEmpty()) {
                ActivityManager.RunningTaskInfo runningTaskInfo = runningTasks.get(0);
                String packageName = runningTaskInfo.topActivity.getPackageName();
                Trace.d("packname = " + packageName + ",,className=" + runningTaskInfo.topActivity.getClassName());
                return packageName.equals("com.foss.fota");
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    private static ShortcutInfo createShortcutInfo(Context context, Intent intent) {
        ShortcutInfo.Builder builder = new ShortcutInfo.Builder(context, context.getString(com.foss.fota.R.string.app_name))
                .setShortLabel(context.getString(com.foss.fota.R.string.app_name))
                .setLongLabel(context.getString(com.foss.fota.R.string.app_name))
                .setIcon(Icon.createWithResource(context, com.foss.fota.R.mipmap.newversion_shortcut));
        if (intent != null) {
            builder.setIntent(intent);
        }
        return builder.build();
    }

    public static void updateShortcut(Context context, int flag) {
        Trace.d("updateShortcut,flag=" + flag);
        try {
            uninstallShortcut(context);
            if (flag == 2) {
                installShortcut(context);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
