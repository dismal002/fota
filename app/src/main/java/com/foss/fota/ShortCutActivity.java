package com.foss.fota;

import android.app.Activity;
import android.os.Bundle;
import com.foss.fota.update.Notice;

public class ShortCutActivity extends Activity {
    @Override // android.app.Activity
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_short_cut);
        Notice.installShortcut(MyApplication.getInstance());
        finish();
    }
}
