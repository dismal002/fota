package com.foss.fota.sysoper;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.ServiceInfo;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemClock;
import android.text.TextUtils;
import com.foss.fota.MyApplication;
import com.foss.fota.sysoper.IRecovery;
import com.foss.fota.sysoper.IRecoveryCallback;
import com.foss.fota.update.EventMessage;
import com.foss.fota.update.Notice;
import com.foss.fota.update.report.ReportData;
import com.foss.fota.utils.Trace;
import com.foss.fota.utils.DeviceUtil;
import com.foss.fota.utils.FileUtil;
import com.foss.fota.utils.PreferencesUtils;
import de.greenrobot.event.EventBus;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.compress.archivers.zip.UnixStat;

/* JADX INFO: loaded from: classes.dex */
public class Recovery {
    private static IRecoveryCallback mCallback;
    private static ServiceConnection serviceConnection;
    private Context context;
    private String mRemotePkgName;
    private String path;
    public static final String FOTA_REMOTE_SERVICE = "com.foss.fota.sysoper.RecoveryService";
    public static final String FOTA_REMOTE_PKG_NAMAE = "com.foss.fota.sysoper";
    private static volatile Recovery mBinder = null;
    private static boolean isAb = false;
    private boolean isApplied = false;
    private boolean isServiceConnected = false;
    private IRecovery myService = null;

    private Recovery(Context context) {
        this.context = context.getApplicationContext();
        this.mRemotePkgName = getRemoteServerName(context, FOTA_REMOTE_SERVICE);
        if (TextUtils.isEmpty(this.mRemotePkgName)) {
            this.mRemotePkgName = FOTA_REMOTE_PKG_NAMAE;
        }
        if (mCallback == null) {
            mCallback = new IRecoveryCallback.Stub() { // from class: com.foss.fota.sysoper.Recovery.1
                @Override // com.foss.fota.sysoper.IRecoveryCallback
                public int onStatusUpdate(int i, float f) {
                    Trace.d("============onStatusUpdate,percent=" + f + ",,state=" + i + ",,show=" + DeviceUtil.getInstance().getFinalizingPro());
                    if (!Recovery.this.isApplied && i == 0) {
                        Recovery.this.applyPayload();
                        return 0;
                    }
                    if (i == 3) {
                        if ("ShowFinalizingPro".equalsIgnoreCase(DeviceUtil.getInstance().getFinalizingPro())) {
                            EventBus.getDefault().post(new EventMessage(300, i, (long) Math.floor(50.0f * f), 0L, "ab"));
                            return 0;
                        }
                        EventBus.getDefault().post(new EventMessage(300, i, (long) Math.floor(100.0f * f), 0L, "ab"));
                        return 0;
                    }
                    if (i == 5 && "ShowFinalizingPro".equalsIgnoreCase(DeviceUtil.getInstance().getFinalizingPro())) {
                        EventBus.getDefault().post(new EventMessage(300, i, ((long) Math.floor(50.0f * f)) + 50, 0L, "ab"));
                        return 0;
                    }
                    if (i == 6) {
                        Recovery.this.abSuccess();
                        return 0;
                    }
                    return 0;
                }

                @Override // com.foss.fota.sysoper.IRecoveryCallback
                public int onPayloadApplicationComplete(int i) {
                    Trace.d("onPayloadApplicationComplete,errCode=" + i);
                    if (i != 0) {
                        if (Recovery.this.isApplied) {
                            Recovery.this.abFail(i, true);
                        } else {
                            Recovery.this.applyPayload();
                        }
                    } else {
                        ReportData.reportInstallResult(MyApplication.getInstance(), true, 0, "ab");
                        Recovery.this.abSuccess();
                    }
                    return 0;
                }
            };
        }
        if (serviceConnection == null) {
            serviceConnection = new ServiceConnection() { // from class: com.foss.fota.sysoper.Recovery.2
                @Override // android.content.ServiceConnection
                public void onServiceDisconnected(ComponentName componentName) {
                    Recovery.this.myService = null;
                    Recovery.this.isServiceConnected = false;
                    Trace.d("onServiceDisconnected ");
                }

                @Override // android.content.ServiceConnection
                public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                    Recovery.this.myService = IRecovery.Stub.asInterface(iBinder);
                    Recovery.this.isServiceConnected = true;
                    if (Recovery.isAb) {
                        Trace.d("onServiceConnected.ab update");
                        Recovery.this.abBind();
                    } else {
                        Trace.d("onServiceConnected");
                    }
                }
            };
        }
    }

    public static Recovery with(Context context) {
        isAb = false;
        if (mBinder == null) {
            synchronized (Recovery.class) {
                if (mBinder == null) {
                    mBinder = new Recovery(context.getApplicationContext());
                }
            }
        }
        return mBinder;
    }

    private Intent getExplicitIntent(Context context, Intent intent) {
        ComponentName componentName = new ComponentName(this.mRemotePkgName, FOTA_REMOTE_SERVICE);
        Intent intent2 = new Intent(intent);
        intent2.setComponent(componentName);
        return intent2;
    }

    public boolean connect() {
        if (this.isServiceConnected) {
            return true;
        }
        try {
            Intent intent = new Intent();
            intent.setAction("android.intent.action.AdupsFota.Recovery");
            Trace.d("connect()," + (intent == null) + ",,," + (getExplicitIntent(this.context, intent) == null));
            return this.context.bindService(new Intent(getExplicitIntent(this.context, intent)), serviceConnection, 1);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public int execute(String str) {
        return exec(str);
    }

    public void executeAb(String str) {
        try {
            if (!this.isServiceConnected) {
                this.isServiceConnected = connect();
            }
            if (!this.isServiceConnected) {
                EventBus.getDefault().post(new EventMessage(300, 100, 0L, 419L, "ab"));
                Trace.d("[execute] connect fail");
            } else {
                this.isApplied = false;
                isAb = true;
                this.path = str;
            }
        } catch (Exception e) {
            Trace.d(e.getMessage());
        }
    }

    public void reboot() {
        int i = 0;
        try {
            if (!this.isServiceConnected) {
                connect();
            }
            while (this.myService == null) {
                SystemClock.sleep(100L);
                i++;
                if (i >= 100) {
                    Trace.d("[execute] timeout");
                    return;
                }
            }
            PreferencesUtils.putInt(MyApplication.getInstance(), "ota_update_status", 0);
            SystemClock.sleep(1000L);
            this.myService.reboot();
            Trace.d("[execute] reboot");
            if (this.myService != null) {
                this.context.unbindService(serviceConnection);
            }
        } catch (Exception e) {
            Trace.d(e.getMessage());
        }
    }

    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:20:0x006a -> B:21:0x0014). Please report as a decompilation issue!!! */
    private int exec(String str) {
        int iRecovery = 0;
        if (!this.isServiceConnected) {
            this.isServiceConnected = connect();
        }
        if (!this.isServiceConnected) {
            Trace.d("[execute] connect fail");
        } else {
            Trace.d("[execute] command = " + str);
            while (true) {
                try {
                    if (this.myService == null) {
                        SystemClock.sleep(100L);
                        iRecovery++;
                        if (iRecovery >= 100) {
                            Trace.d("[execute] timeout");
                            iRecovery = -4;
                            break;
                        }
                    } else {
                        iRecovery = this.myService.recovery(str);
                        Trace.d("[execute] result : " + iRecovery);
                        if (this.myService != null) {
                            this.context.unbindService(serviceConnection);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    iRecovery = -3;
                }
            }
        }
        return iRecovery;
    }

    private String getRemoteServerName(Context context, String str) {
        List<PackageInfo> installedPackages = context.getPackageManager().getInstalledPackages(4);
        if (installedPackages != null && installedPackages.size() > 0) {
            for (PackageInfo packageInfo : installedPackages) {
                ServiceInfo[] serviceInfoArr = packageInfo.services;
                if (serviceInfoArr != null && serviceInfoArr.length > 0 && (packageInfo.applicationInfo.flags & 1) > 0) {
                    for (ServiceInfo serviceInfo : serviceInfoArr) {
                        if (str.equals(serviceInfo.name)) {
                            return packageInfo.packageName;
                        }
                    }
                }
            }
        }
        return null;
    }

    public void abBind() {
        Trace.d("abBind enter," + (this.myService != null));
        try {
            if (this.myService != null) {
                this.myService.recovery_ab_binder(mCallback);
            } else {
                abFail(419, false);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void applyPayload() {
        Trace.d("applyPayload enter");
        new Thread(new Runnable() { // from class: com.foss.fota.sysoper.Recovery.3
            @Override // java.lang.Runnable
            public void run() {
                try {
                    if (Recovery.this.myService == null || TextUtils.isEmpty(Recovery.this.path) || !new File(Recovery.this.path).exists() || !Recovery.isAb) {
                        if (Recovery.isAb) {
                            Recovery.this.abFail(UnixStat.DEFAULT_FILE_PERM, false);
                        }
                    } else {
                        List<String> listB = FileUtil.b(Recovery.this.path);
                        String[] strArr = new String[listB.size()];
                        String[] strArr2 = (String[]) listB.toArray(strArr);
                        Trace.d("headerKeyValuePairs=" + Arrays.toString(strArr2) + ",," + strArr.length);
                        if (strArr2.length <= 1) {
                            Recovery.this.abFail(418, false);
                        } else {
                            EventBus.getDefault().post(new EventMessage(300, 99, 0L, 5L, "ab"));
                            PreferencesUtils.putInt(MyApplication.getInstance(), "ota_update_status", 5);
                            Recovery.this.isApplied = true;
                            Recovery.this.myService.recovery_ab_install(new RecoveryParams("file://" + Recovery.this.path, FileUtil.a(Recovery.this.path), Long.parseLong(strArr2[1].replace("FILE_SIZE=", "")), strArr2));
                        }
                    }
                } catch (Throwable e) {
                    Trace.d(e.getMessage());
                }
            }
        }).start();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void abFail(int i, boolean z) {
        Trace.d("abFail,enter");
        if (i == 20) {
            PreferencesUtils.putBoolean(MyApplication.getInstance(), "rom_damaged", true);
        }
        PreferencesUtils.putInt(MyApplication.getInstance(), "ota_update_status", 0);
        ReportData.reportInstallResult(MyApplication.getInstance(), false, i, "ab");
        if (z) {
            EventBus.getDefault().post(new EventMessage(300, i, 0L, 1L, "ab"));
        } else {
            EventBus.getDefault().post(new EventMessage(300, 100, 0L, i, "ab"));
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void abSuccess() {
        Trace.d("abSuccess enter");
        PreferencesUtils.putInt(MyApplication.getInstance(), "ota_update_status", 6);
        EventBus.getDefault().post(new EventMessage(300, 0, 0L, 1L, "ab"));
        PreferencesUtils.putBoolean(MyApplication.getInstance(), "ota_enter_recovery", true);
        com.foss.fota.update.install.Install.d(MyApplication.getInstance());
        if (PreferencesUtils.b(MyApplication.getInstance(), "ota_update_local", false)) {
            reboot();
        }
    }
}
