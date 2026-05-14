package com.foss.fota;

import android.os.Bundle;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import com.foss.fota.utils.Trace;
import com.foss.fota.utils.DeviceUtil;

/* JADX INFO: loaded from: classes.dex */
public abstract class BaseActivity extends AppCompatActivity implements View.OnClickListener {
    DrawerLayout drawerLayout;
    private int activityStatus;
    private boolean noTitle = true;

    protected abstract void initData();

    protected abstract void widgetClick(View view);

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        widgetClick(view);
    }

    @Override // androidx.appcompat.app.AppCompatActivity, androidx.fragment.app.FragmentActivity, androidx.core.app.ComponentActivity, android.app.Activity
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Trace.d("");
        if ("1".equals(DeviceUtil.getInstance().getDisplay())) {
            setRequestedOrientation(0);
        } else if ("2".equals(DeviceUtil.getInstance().getDisplay())) {
            setRequestedOrientation(4);
        } else {
            setRequestedOrientation(1);
        }
        if (this.noTitle) {
            requestWindowFeature(1);
        }
        initData();
    }

    @Override // androidx.appcompat.app.AppCompatActivity, androidx.fragment.app.FragmentActivity, android.app.Activity
    protected void onStart() {
        super.onStart();
        Trace.d("");
    }

    @Override // androidx.fragment.app.FragmentActivity, android.app.Activity
    protected void onResume() {
        super.onResume();
        this.activityStatus = 0;
        Trace.d("");
    }

    @Override // androidx.appcompat.app.AppCompatActivity, androidx.fragment.app.FragmentActivity, android.app.Activity
    protected void onStop() {
        super.onStop();
        this.activityStatus = 1;
        Trace.d("");
    }

    @Override // androidx.fragment.app.FragmentActivity, android.app.Activity
    protected void onPause() {
        super.onPause();
        this.activityStatus = 2;
        Trace.d("");
    }

    @Override // android.app.Activity
    protected void onRestart() {
        super.onRestart();
        Trace.d("");
    }

    @Override // androidx.appcompat.app.AppCompatActivity, androidx.fragment.app.FragmentActivity, android.app.Activity
    protected void onDestroy() {
        super.onDestroy();
        this.activityStatus = 3;
        Trace.d("");
    }

    public void openDrawer() {
        if (this.drawerLayout != null) {
            this.drawerLayout.openDrawer(GravityCompat.START);
        }
    }
}
