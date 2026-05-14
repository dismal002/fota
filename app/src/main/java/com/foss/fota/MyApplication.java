package com.foss.fota;

import android.app.Application;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import com.foss.fota.utils.Trace;
import com.foss.fota.utils.CrashHandler;
import com.foss.fota.utils.DeviceInfoProvider;
import com.foss.fota.utils.OkHttpUtil;
import com.foss.fota.utils.PreferencesUtils;
import com.foss.fota.utils.StorageUtil;

public class MyApplication extends Application {
    private static Context context;

    public static Context getInstance() {
        return context;
    }

    public static boolean isPrivacyPolicyInstalled() {
        ApplicationInfo applicationInfo;
        try {
            // "com.foss.privacypolicy" assumed for MaterialDialog.a.e
            applicationInfo = context.getPackageManager().getApplicationInfo("com.foss.privacypolicy", 0);
        } catch (Exception e) {
            applicationInfo = null;
        }
        return applicationInfo != null;
    }

    public static boolean isRejectStatus() {
        return PreferencesUtils.getBoolean(context, "reject_status", false);
    }

    public static void setRejectStatus(boolean z) {
        PreferencesUtils.putBoolean(context, "reject_status", z);
    }

    public static boolean isImeiSupported() {
        return isPrivacyPolicyInstalled() && isRejectStatus();
    }

    public static void updatePrivacyStatus() {
        if (isPrivacyPolicyInstalled()) {
            try {
                String type = context.getContentResolver().getType(Uri.parse("content://com.foss.privacypolicy.MyContentProvider/reject_status"));
                setRejectStatus(Boolean.valueOf(type).booleanValue());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        PreferencesUtils.putBoolean(context, "connect_net", (isPrivacyPolicyInstalled() && isRejectStatus()) ? false : true);
    }

    public static boolean isNoReport() {
        return PreferencesUtils.getBoolean(context, "no_report", false);
    }

    public static void setNoReport(boolean z) {
        PreferencesUtils.putBoolean(context, "no_report", z);
    }

    @Override // android.app.Application
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        Trace.d("application create");
        try {
            registerCoreReceivers();
            StorageUtil.init(this);
            CrashHandler.getInstance().init(this);
            OkHttpUtil.clearDnsCache();
        } catch (Exception e) {
            e.printStackTrace();
        }
        String checkUrl = PreferencesUtils.getString(this, "check_url");
        Trace.d("initUrl = " + checkUrl);
        // "http://rebootv5.foss.com" assumed for com.foss.fota.config.ServerApi.PRIMARY_DOMAIN
        if (!TextUtils.isEmpty(checkUrl) && !checkUrl.equals("http://rebootv5.foss.com")) {
            PreferencesUtils.putString(context, "check_url", "http://rebootv5.foss.com");
        }
        DeviceInfoProvider.getInstance(this).getDeviceId(this);
        updatePrivacyStatus();
    }

    private void registerCoreReceivers() {
        if (Build.VERSION.SDK_INT >= 24) {
            IntentFilter intentFilter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
            intentFilter.addAction("android.intent.action.ACTION_POWER_DISCONNECTED");
            intentFilter.addAction("android.intent.action.DATE_CHANGED");
            registerReceiver(new MyReceiver(), intentFilter);
        }
    }
    
    // Compatibility aliases
    public static Context a() { return getInstance(); }
    public static boolean b() { return isPrivacyPolicyInstalled(); }
    public static boolean c() { return isRejectStatus(); }
    public static boolean d() { return isImeiSupported(); }
    public static boolean f() { return isNoReport(); }
}
