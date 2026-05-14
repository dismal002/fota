package com.foss.fota;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import com.foss.fota.MaterialDialog;
import com.foss.fota.utils.PreferencesUtils;

/* JADX INFO: loaded from: classes.dex */
public class FotaPopInstallWindow extends Activity {
    @Override // android.app.Activity
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_fota_pop_install_window);
        a();
    }

    @Override // android.app.Activity
    protected void onDestroy() {
        super.onDestroy();
    }

    private void a() {
        new MaterialDialog.Builder(this).contentLayout(R.string.pop_install_remind_title).a(R.string.pop_install_remind_content).footerLayout(R.string.update_now).a(new MaterialDialog.DialogActionListener() { // from class: com.foss.fota.FotaPopInstallWindow.2
            @Override // com.foss.fota.MaterialDialog.DialogActionListener
            public void a(MaterialDialog aVar, DialogAction dialogAction) {
                FotaPopInstallWindow.this.b();
            }
        }).a(new DialogInterface.OnDismissListener() { // from class: com.foss.fota.FotaPopInstallWindow.1
            @Override // android.content.DialogInterface.OnDismissListener
            public void onDismiss(DialogInterface dialogInterface) {
                FotaPopInstallWindow.this.finish();
            }
        }).c();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void b() {
        c();
        startActivity(new Intent(this, (Class<?>) GoogleOtaClient.class));
    }

    private void c() {
        long jC = PreferencesUtils.c(this, "ota_install_delay_schedule");
        if (jC > 0) {
            com.foss.fota.update.Alarm.c(this, jC + System.currentTimeMillis());
        }
    }
}
