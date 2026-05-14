package com.foss.fota;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.core.app.NotificationCompat;
import android.text.Html;
import com.foss.fota.MaterialDialog;
import com.foss.fota.update.query.QueryInfo;
import com.foss.fota.update.model.LanguageModel;
import com.foss.fota.update.model.VersionModel;
import java.util.ArrayList;

/* JADX INFO: loaded from: classes.dex */
public class FotaPopWindow extends Activity {
    private MaterialDialog materialDialog;

    @Override // android.app.Activity
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_fota_pop_window);
        a(getIntent());
    }

    private void a(Intent intent) {
        int intExtra = intent.getIntExtra(NotificationCompat.CATEGORY_STATUS, -1);
        VersionModel versionModelA = QueryInfo.getInstance(this).getVersionModel();
        if (versionModelA != null) {
            MaterialDialog.Builder c0015aA = new MaterialDialog.Builder(this).b(R.string.sdCard_upgrade_hint).a(Html.fromHtml(a(versionModelA)).toString()).a(new MaterialDialog.DialogActionListener() { // from class: com.foss.fota.FotaPopWindow.2
                @Override // com.foss.fota.MaterialDialog.DialogActionListener
                public void a(MaterialDialog aVar, DialogAction dialogAction) {
                    FotaPopWindow.this.startActivity(new Intent(FotaPopWindow.this, (Class<?>) GoogleOtaClient.class));
                }
            }).a(new DialogInterface.OnDismissListener() { // from class: com.foss.fota.FotaPopWindow.1
                @Override // android.content.DialogInterface.OnDismissListener
                public void onDismiss(DialogInterface dialogInterface) {
                    FotaPopWindow.this.finish();
                }
            });
            if (intExtra == 1) {
                c0015aA.e(R.string.btn_download);
                this.materialDialog = c0015aA.getStatus();
                this.materialDialog.show();
                return;
            } else {
                if (intExtra == 4) {
                    c0015aA.e(R.string.update_now);
                    this.materialDialog = c0015aA.getStatus();
                    this.materialDialog.show();
                    return;
                }
                finish();
                return;
            }
        }
        finish();
    }

    private String a(VersionModel versionModel) {
        int i;
        if (versionModel != null) {
            String language = getResources().getConfiguration().locale.getLanguage();
            String str = "zh".equals(language) ? language + "_" + getResources().getConfiguration().locale.getCountry() : language;
            ArrayList<LanguageModel> releasenotes = versionModel.getReleasenotes();
            if (releasenotes != null && releasenotes.size() > 0) {
                int size = releasenotes.size();
                if (size == 1) {
                    return releasenotes.get(0).getContent().replaceAll("#FFFFFF", "#434343");
                }
                int i2 = 0;
                while (true) {
                    if (i2 >= size) {
                        i = 0;
                        break;
                    }
                    if (str.contains(releasenotes.get(i2).getCountry())) {
                        i = i2;
                        break;
                    }
                    i2++;
                }
                return releasenotes.get(i).getContent().replaceAll("#FFFFFF", "#434343");
            }
        }
        return "";
    }
}
