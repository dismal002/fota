package com.foss.fota.update.install;

import android.content.Context;
import android.text.TextUtils;
import com.foss.fota.MyApplication;
import com.foss.fota.utils.Trace;
import com.foss.fota.utils.PreferencesUtils;
import com.foss.fota.utils.SecurityUtil;
import com.foss.fota.utils.StorageUtil;
import java.io.File;

public class InstallParser2 extends InstallParserBase {
    @Override // com.foss.fota.update.install.InstallParserBase
    public int a(Context context, String str, String str2) {
        String strB;
        Exception e;
        Trace.d("InstallParser2", "[verify]:  start    ");
        try {
            if (!new File(StorageUtil.g(context)).exists()) {
                Trace.d("InstallParser2", "[verify]:  deltaPath is null");
                return 401;
            }
            Trace.d("InstallParser2", "[verify]:  zipFilePath = " + str);
            Trace.d("InstallParser2", "[verify]:  server sha256  = " + str2);
            try {
                strB = SecurityUtil.b(str);
            } catch (Exception e2) {
                strB = null;
                e = e2;
            }
            try {
                PreferencesUtils.putString(context, "sha", strB);
            } catch (Exception e3) {
                e = e3;
                e.printStackTrace();
            }
            if (TextUtils.isEmpty(strB)) {
                Trace.d("InstallParser2", "[verify]: get file sha256 error!");
                return 416;
            }
            Trace.d("InstallParser2", "[verify]:  file sha256  = " + strB);
            if (strB == null || !strB.equalsIgnoreCase(str2)) {
                if (!strB.equalsIgnoreCase(str2)) {
                    com.foss.fota.update.install.InstallResult.b(MyApplication.getInstance(), false);
                }
                Trace.d("InstallParser2", "[verify]:  sha256 is different");
                return 402;
            }
            Trace.d("InstallParser2", "[verify]:  sha256 is equal");
            String strA = com.foss.fota.update.query.QueryRootVerify.a(str);
            Trace.d("InstallParser2", "[verify]: isRomDamaged  result = " + strA);
            if (!TextUtils.isEmpty(strA)) {
                Trace.d("InstallParser2", "[verify]: rom  are damaged ");
                a(strA);
                PreferencesUtils.putBoolean(context, "rom_damaged", true);
                PreferencesUtils.putInt(context, "rom_damaged_version", com.foss.fota.utils.DeviceUtil.getInstance().getVersion());
                if (PreferencesUtils.b(context, "isupgrade", 0) == 1) {
                    Trace.d("InstallParser2", "[verify]: rom  are damaged, upgrade == 1  ");
                    return 404;
                }
            }
            Trace.d("InstallParser2", "[verify]: finish  success");
            return 405;
        } catch (Exception e4) {
            return 401;
        }
    }
}
