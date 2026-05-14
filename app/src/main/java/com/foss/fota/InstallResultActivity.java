package com.foss.fota;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import com.foss.fota.MaterialDialog;
import com.foss.fota.utils.Trace;

public class InstallResultActivity extends BaseActivity {
    String b;

    @Override // com.foss.fota.BaseActivity
    protected void initData() {
    }

    @Override // com.foss.fota.BaseActivity
    protected void widgetClick(View view) {
    }

    @Override // com.foss.fota.BaseActivity, androidx.appcompat.app.AppCompatActivity, androidx.fragment.app.FragmentActivity, androidx.core.app.ComponentActivity, android.app.Activity
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_update_result);
        this.b = getIntent().getStringExtra("version");
        Trace.d("[onCreate] version name = " + this.b);
        new Handler().post(new Runnable() { // from class: com.foss.fota.InstallResultActivity.1
            @Override // java.lang.Runnable
            public void run() {
                InstallResultActivity.this.g();
            }
        });
    }

    public void g() {
        Trace.d("[showResultDialog] ============");
        new MaterialDialog.Builder(this).b(R.string.updateSuccessTitle).a(getString(R.string.updateSuccess, new Object[]{""})).e(R.string.ota_button_text_know).a(new MaterialDialog.DialogActionListener() { // from class: com.foss.fota.InstallResultActivity.2
            @Override
            public void a(MaterialDialog dialog, DialogAction dialogAction) {
                InstallResultActivity.this.finish();
            }
        }).a(false).c();
    }

    @Override // com.foss.fota.BaseActivity, androidx.appcompat.app.AppCompatActivity, androidx.fragment.app.FragmentActivity, android.app.Activity
    protected void onStop() {
        super.onStop();
    }
}
