package com.foss.fota.update.install;

import android.content.Context;
import android.os.PowerManager;
import android.text.TextUtils;
import com.foss.fota.MyApplication;
import com.foss.fota.utils.Trace;
import com.foss.fota.utils.FileUtil;
import com.foss.fota.utils.PreferencesUtils;
import com.foss.fota.utils.StorageUtil;
import java.io.File;

public class InstallParser1 extends InstallParserBase {
    @Override // com.foss.fota.update.install.InstallParserBase
    public int a(Context context, String str, String str2) {
        Trace.d("InstallParser1", "[verify]: start ");
        if (!Install.handlePostDownloadTask(context)) {
            return 412;
        }
        if (!FileUtil.b(StorageUtil.g(context), StorageUtil.getUpdateStatus(context) + "/package.zip")) {
            Trace.d("InstallParser1", "[verify]: rename  fail ");
            return 408;
        }
        Trace.d("InstallParser1", "[verify]: rename  success ");
        File file = new File(StorageUtil.getUpdateStatus(context) + "/package.zip");
        String parent = file.getParent();
        if (parent == null) {
            Trace.d("InstallParser1", "[verify]: deltaPath  null ");
            return 410;
        }
        PowerManager.WakeLock wakeLockNewWakeLock = ((PowerManager) context.getSystemService("power")).newWakeLock(1, "unzipwakeup");
        wakeLockNewWakeLock.acquire();
        boolean zA = FileUtil.a(file, parent);
        if (wakeLockNewWakeLock != null) {
            wakeLockNewWakeLock.release();
        }
        if (!zA) {
            Trace.d("InstallParser1", "[verify]: unzip  fail ");
            return 401;
        }
        Trace.d("InstallParser1", "[verify]: unzip  success ");
        String strG = FileUtil.g(parent + "/update.zip");
        String strH = FileUtil.h(parent + "/md5sum");
        if (strG == null) {
            return 411;
        }
        if (!strG.equalsIgnoreCase(strH)) {
            com.foss.fota.update.install.InstallResult.b(MyApplication.getInstance(), false);
            return 402;
        }
        Trace.d("InstallParser1", "[verify]: md5  equal ");
        FileUtil.b(context, parent);
        String strA = com.foss.fota.update.query.QueryRootVerify.a(parent + "/update.zip");
        Trace.d("InstallParser1", "[verify]: isRomDamaged  result = " + strA);
        if (!TextUtils.isEmpty(strA)) {
            a(strA);
            PreferencesUtils.putBoolean(context, "rom_damaged", true);
            PreferencesUtils.putInt(context, "rom_damaged_version", com.foss.fota.utils.DeviceUtil.getInstance().getVersion());
            Trace.d("InstallParser1", "[verify]: rom  are damaged  ");
            if (PreferencesUtils.b(context, "isupgrade", 0) == 1) {
                Trace.d("InstallParser1", "[verify]: rom  are damaged, upgrade == 1  ");
                return 404;
            }
        }
        Trace.d("InstallParser1", "[verify]: finish  success");
        return 405;
    }
}
